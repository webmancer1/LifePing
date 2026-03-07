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
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.lifeping.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext

data class DeadlineInfo(
    val lastCheckInTime: LocalDateTime,
    val targetTime: LocalDateTime,
    val intervalMs: Long
)

@Singleton
class CheckInManager @Inject constructor(
    private val checkInDao: CheckInDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workManager: WorkManager,
    @ApplicationContext private val context: Context
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
        
        scheduleNotifications()
    }
    
    private suspend fun scheduleNotifications() {
        val interval = userPreferencesRepository.checkInInterval.first()
        val gracePeriod = userPreferencesRepository.gracePeriod.first()
        val notifyUpcoming = userPreferencesRepository.notifyUpcoming.first()
        val autoAlert = userPreferencesRepository.autoAlert.first()
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        // Target times
        val latest = checkInDao.getLatestCheckIn().first()
        val baseTimeStr = userPreferencesRepository.baseCheckInTime.first()
        val lastTime = if (baseTimeStr != null) {
             LocalDateTime.parse(baseTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
         } else if (latest != null) {
             LocalDateTime.parse(latest.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
         } else {
             LocalDateTime.now()
         }
        
        val targetTimeMs = lastTime.plusNanos(interval * 1_000_000).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val warningTimeMs = targetTimeMs - (5 * 60 * 1000L) // 5 minutes before
        val missedTimeMs = targetTimeMs + gracePeriod // Grace period end
        
        val nowMs = System.currentTimeMillis()
        
        // Cancel existing
        val warningIntent = Intent(context, AlarmReceiver::class.java).apply { putExtra(AlarmReceiver.EXTRA_ALARM_TYPE, AlarmReceiver.TYPE_WARNING) }
        val checkInIntent = Intent(context, AlarmReceiver::class.java).apply { putExtra(AlarmReceiver.EXTRA_ALARM_TYPE, AlarmReceiver.TYPE_CHECK_IN) }
        val missedIntent = Intent(context, AlarmReceiver::class.java).apply { putExtra(AlarmReceiver.EXTRA_ALARM_TYPE, AlarmReceiver.TYPE_MISSED) }

        val warningPending = PendingIntent.getBroadcast(context, AlarmReceiver.TYPE_WARNING.hashCode(), warningIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val checkInPending = PendingIntent.getBroadcast(context, AlarmReceiver.TYPE_CHECK_IN.hashCode(), checkInIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val missedPending = PendingIntent.getBroadcast(context, AlarmReceiver.TYPE_MISSED.hashCode(), missedIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        
        alarmManager.cancel(warningPending)
        alarmManager.cancel(checkInPending)
        alarmManager.cancel(missedPending)

        // Schedule new ones if they are in the future
        try {
            if (notifyUpcoming && warningTimeMs > nowMs) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, warningTimeMs, warningPending)
            }
            if (notifyUpcoming && targetTimeMs > nowMs) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetTimeMs, checkInPending)
            }
            if (autoAlert && missedTimeMs > nowMs) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, missedTimeMs, missedPending)
            }
        } catch (e: SecurityException) {
            android.util.Log.e("CheckInManager", "Exact alarm permission missing", e)
        }
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
