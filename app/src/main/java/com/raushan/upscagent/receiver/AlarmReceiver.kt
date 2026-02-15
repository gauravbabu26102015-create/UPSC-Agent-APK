package com.raushan.upscagent.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.raushan.upscagent.service.AlarmService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val subject = intent.getStringExtra("subject") ?: "Study"
        val message = intent.getStringExtra("message") ?: "Time to study!"
        val type = intent.getStringExtra("type") ?: "start"
        val alarmId = intent.getIntExtra("alarm_id", 0)

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("subject", subject)
            putExtra("message", message)
            putExtra("type", type)
            putExtra("alarm_id", alarmId)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
