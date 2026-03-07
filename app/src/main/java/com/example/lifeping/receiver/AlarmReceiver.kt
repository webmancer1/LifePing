package com.example.lifeping.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.lifeping.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    companion object {
        const val EXTRA_ALARM_TYPE = "EXTRA_ALARM_TYPE"

        const val TYPE_WARNING = "TYPE_WARNING"
        const val TYPE_CHECK_IN = "TYPE_CHECK_IN"
        const val TYPE_MISSED = "TYPE_MISSED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmType = intent.getStringExtra(EXTRA_ALARM_TYPE)
        
        android.util.Log.d("AlarmReceiver", "Received alarm of type: $alarmType")

        when (alarmType) {
            TYPE_WARNING -> notificationHelper.showWarningNotification()
            TYPE_CHECK_IN -> notificationHelper.showCheckInTimeNotification()
            TYPE_MISSED -> notificationHelper.showMissedNotification()
        }
    }
}
