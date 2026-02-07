package com.example.lifeping.data.repository

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lifeping.data.local.CheckInDao
import com.example.lifeping.data.model.CheckIn
import com.example.lifeping.data.preferences.UserPreferencesRepository
import com.example.lifeping.worker.MissedCheckInWorker
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckInManager @Inject constructor(
    private val checkInDao: CheckInDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workManager: WorkManager
) {

    suspend fun performCheckIn() {
        val now = LocalDateTime.now()
        val checkIn = CheckIn(
            timestamp = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
        checkInDao.insertCheckIn(checkIn)
        
        scheduleMissedCheckInDeadline()
    }

    suspend fun scheduleMissedCheckInDeadline() {
        val interval = userPreferencesRepository.checkInInterval.first()
        val gracePeriod = userPreferencesRepository.gracePeriod.first()
        
        val totalDelay = interval + gracePeriod
        
        val workRequest = OneTimeWorkRequestBuilder<MissedCheckInWorker>()
            .setInitialDelay(totalDelay, TimeUnit.MILLISECONDS)
            .addTag("missed_check_in_escalation")
            .build()

        workManager.enqueueUniqueWork(
            "missed_check_in_protection",
            ExistingWorkPolicy.REPLACE, // Restart timer on every check-in
            workRequest
        )
        
        android.util.Log.d("CheckInManager", "Scheduled escalation in ${totalDelay / 1000 / 60} minutes")
    }

    suspend fun getNextDeadline(): LocalDateTime {
         val latest = checkInDao.getLatestCheckIn().first()
         val interval = userPreferencesRepository.checkInInterval.first()
         
         return if (latest != null) {
             val lastTime = LocalDateTime.parse(latest.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
             lastTime.plusNanos(interval * 1_000_000) // millis to nanos
         } else {
             // No check-in yet, deadline is now + interval (assuming fresh start)
             LocalDateTime.now().plusNanos(interval * 1_000_000)
         }
    }

    suspend fun resetAllData() {
        // 1. Clear all history
        checkInDao.deleteAllCheckIns()
        
        // 2. Reschedule based on NEW settings (which are already saved in repo before calling this)
        // Since we deleted history, the "Next Deadline" logic in getNextDeadline() will fallback to LocalDateTime.now() + interval
        // But scheduleMissedCheckInDeadline() uses a OneTimeWorkRequest with initialDelay = interval + grace
        // This effectively restarts the "clock" from NOW.
        scheduleMissedCheckInDeadline()
    }
}
