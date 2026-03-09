package com.example.lifeping.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lifeping.data.local.CheckInDao
import com.example.lifeping.data.model.CheckIn
import com.example.lifeping.data.model.CheckInStatus
import com.example.lifeping.data.preferences.UserPreferencesRepository
import com.example.lifeping.data.repository.CheckInManager
import com.example.lifeping.data.repository.ContactRepository
import com.example.lifeping.util.NotificationSender
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@HiltWorker
class MissedCheckInWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val checkInDao: CheckInDao,
    private val contactRepository: ContactRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val checkInManager: CheckInManager,
    private val notificationSender: NotificationSender
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        android.util.Log.d("MissedCheckInWorker", "Checking for missed check-in...")

        val latestCheckIn = checkInDao.getLatestCheckIn().first()
        val interval = userPreferencesRepository.checkInInterval.first()
        val gracePeriod = userPreferencesRepository.gracePeriod.first()
        
        val now = LocalDateTime.now()
        val timeoutThreshold = interval + gracePeriod

        if (latestCheckIn != null) {
            val lastTime = try {
                LocalDateTime.parse(latestCheckIn.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            } catch (e: Exception) {
                // Fallback or error handling
                LocalDateTime.MIN
            }

            val timeSinceLastcheckIn = ChronoUnit.MILLIS.between(lastTime, now)
            
            // Double check: if time since last check-in is LESS than threshold, we are good.
            // This might happen if user checked in *just* before worker fired.
            if (timeSinceLastcheckIn < timeoutThreshold) {
                android.util.Log.d("MissedCheckInWorker", "False alarm. User checked in recently.")
                return Result.success()
            }
        }

        // If we are here, TRULY MISSED.
        android.util.Log.d("MissedCheckInWorker", "Check-in truly missed. Recording missed check-in.")
        
        // Record the missed check-in in the database
        val isoFormat = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val missedCheckIn = CheckIn(
            timestamp = isoFormat,
            status = CheckInStatus.MISSED
        )
        checkInDao.insertCheckIn(missedCheckIn)
        
        // Also update the base check-in time so the timer restarts from this missed point
        userPreferencesRepository.saveBaseCheckInTime(isoFormat)
        checkInManager.scheduleMissedCheckInDeadline()

        triggerEscalation()

        return Result.success()
    }

    private suspend fun triggerEscalation() {
        val contacts = contactRepository.getAllContacts().first()
        
        if (contacts.isEmpty()) {
            android.util.Log.w("MissedCheckInWorker", "No contacts to notify!")
            return
        }

        contacts.forEach { contact ->
            android.util.Log.e("LifePingEscalation", "ALERT: User missed check-in! Notifying ${contact.name} (${contact.phoneNumber})")
            
            val messageText = "URGENT: I have missed my LifePing check-in and failed to respond during the grace period. Please check on me."
            
            if (contact.notifyViaSms) {
                notificationSender.sendSms(contact.phoneNumber, messageText)
            }
            if (contact.notifyViaEmail && contact.email.isNotBlank()) {
                notificationSender.sendEmail(contact.email, "LifePing Emergency Alert", messageText)
            }
            if (contact.notifyViaWhatsapp) {
                notificationSender.sendWhatsApp(contact.phoneNumber, messageText)
            }
        }
    }
}
