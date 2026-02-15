package com.raushan.upscagent.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.raushan.upscagent.data.repository.AppRepository
import com.raushan.upscagent.utils.AlarmHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val repository = AppRepository(context)
            CoroutineScope(Dispatchers.IO).launch {
                val enabledAlarms = repository.getEnabledAlarms()
                for (alarm in enabledAlarms) {
                    AlarmHelper.scheduleAlarm(context, alarm)
                }
            }
        }
    }
}
