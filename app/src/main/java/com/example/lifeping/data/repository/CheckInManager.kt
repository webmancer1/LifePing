package com.example.lifeping.data.repository

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lifeping.data.local.CheckInDao
import com.example.lifeping.data.model.CheckIn
import com.example.lifeping.data.preferences.UserPreferencesRepository
import com.example.lifeping.worker.MissedCheckInWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class DeadlineInfo(
    val lastCheckInTime: LocalDateTime,
    val targetTime: LocalDateTime,
    val intervalMs: Long
)

@Singleton
class CheckInManager @Inject constructor(
    private val checkInDao: CheckInDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workManager: WorkManager
) {

    suspend fun performCheckIn() {
        val now = LocalDateTime.now()
        val isoFormat = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val checkIn = CheckIn(
            timestamp = isoFormat
        )
        checkInDao.insertCheckIn(checkIn)
        userPreferencesRepository.saveBaseCheckInTime(isoFormat)
        
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
         val baseTimeStr = userPreferencesRepository.baseCheckInTime.first()
         val latest = checkInDao.getLatestCheckIn().first()
         val interval = userPreferencesRepository.checkInInterval.first()
         
         val lastTime = if (baseTimeStr != null) {
             LocalDateTime.parse(baseTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
         } else if (latest != null) {
             LocalDateTime.parse(latest.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
         } else {
             LocalDateTime.now()
         }
         return lastTime.plusNanos(interval * 1_000_000) // millis to nanos
    }

    fun getNextDeadlineFlow(): Flow<DeadlineInfo> {
        return combine(
            userPreferencesRepository.baseCheckInTime,
            checkInDao.getLatestCheckIn(),
            userPreferencesRepository.checkInInterval
        ) { baseTimeStr, latest, interval ->
            val lastTime = if (baseTimeStr != null) {
                LocalDateTime.parse(baseTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            } else if (latest != null) {
                LocalDateTime.parse(latest.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            } else {
                LocalDateTime.now()
            }
            val targetTime = lastTime.plusNanos(interval * 1_000_000)
            DeadlineInfo(lastTime, targetTime, interval)
        }
    }

    suspend fun resetAllData() {
        // 1. Clear all history
        checkInDao.deleteAllCheckIns()
        
        // 2. Set base check-in time to now so timer restarts
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        userPreferencesRepository.saveBaseCheckInTime(now)
        
        // 3. Reschedule based on NEW settings (which are already saved in repo before calling this)
        scheduleMissedCheckInDeadline()
    }

    suspend fun resetHistoryDataOnly() {
        // Ensure base check in time is saved if it hasn't been yet to prevent countdown drop
        val currentBaseTime = userPreferencesRepository.baseCheckInTime.first()
        if (currentBaseTime == null) {
            val latest = checkInDao.getLatestCheckIn().first()
            if (latest != null) {
                userPreferencesRepository.saveBaseCheckInTime(latest.timestamp)
            } else {
                userPreferencesRepository.saveBaseCheckInTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            }
        }
        
        // Clear history. Target time next check in remains identical.
        checkInDao.deleteAllCheckIns()
    }
}
