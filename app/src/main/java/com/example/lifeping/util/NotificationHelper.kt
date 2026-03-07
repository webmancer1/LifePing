package com.example.lifeping.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.lifeping.MainActivity
import com.example.lifeping.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "lifeping_channel_id"
        const val CHANNEL_NAME = "LifePing Notifications"

        // Notification Types
        const val WARNING_NOTIFICATION_ID = 1001
        const val CHECK_IN_NOTIFICATION_ID = 1002
        const val MISSED_NOTIFICATION_ID = 1003
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Check-in reminders and alerts"
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getMainActivityPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun showWarningNotification() {
        showNotification(
            WARNING_NOTIFICATION_ID,
            "Check-in Warning",
            "Your next check-in is in 5 minutes."
        )
    }

    fun showCheckInTimeNotification() {
        showNotification(
            CHECK_IN_NOTIFICATION_ID,
            "Time to Check-in",
            "It is time for your scheduled check-in. Please open the app."
        )
    }

    fun showMissedNotification() {
        showNotification(
            MISSED_NOTIFICATION_ID,
            "Check-in Missed",
            "You missed your check-in and grace period. Emergency contacts may be notified."
        )
    }

    private fun showNotification(id: Int, title: String, content: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Fallback as we don't know if ic_notification exists
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getMainActivityPendingIntent())
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        try {
           notificationManager.notify(id, builder.build())
        } catch(e: SecurityException) {
           android.util.Log.e("NotificationHelper", "Permission denial: POST_NOTIFICATIONS may be missing", e)
        }
    }
}
