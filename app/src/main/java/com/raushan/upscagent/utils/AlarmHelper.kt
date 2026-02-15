package com.raushan.upscagent.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.raushan.upscagent.data.model.StudyAlarm
import com.raushan.upscagent.receiver.AlarmReceiver
import java.util.Calendar

object AlarmHelper {

    fun scheduleAlarm(context: Context, alarm: StudyAlarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Schedule START alarm (when subject starts)
        val startIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
            putExtra("subject", alarm.subject)
            putExtra("type", "start")
            putExtra("message", "Time for ${alarm.subject}! Your study session is starting now.")
            putExtra("color", alarm.colorHex)
        }

        val startPendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id * 10, // Unique request code for start
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule END alarm (when subject ends)
        val endIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
            putExtra("subject", alarm.subject)
            putExtra("type", "end")
            putExtra("message", "${alarm.subject} session complete! Great work! 💪")
            putExtra("color", alarm.colorHex)
        }

        val endPendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id * 10 + 1, // Unique request code for end
            endIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate next trigger time for START
        val startCalendar = getNextAlarmTime(alarm.startHour, alarm.startMinute, alarm.daysOfWeek)
        // Calculate next trigger time for END
        val endCalendar = getNextAlarmTime(alarm.endHour, alarm.endMinute, alarm.daysOfWeek)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(startCalendar.timeInMillis, startPendingIntent),
                        startPendingIntent
                    )
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        endCalendar.timeInMillis,
                        endPendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        startCalendar.timeInMillis,
                        startPendingIntent
                    )
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        endCalendar.timeInMillis,
                        endPendingIntent
                    )
                }
            } else {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(startCalendar.timeInMillis, startPendingIntent),
                    startPendingIntent
                )
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    endCalendar.timeInMillis,
                    endPendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback to inexact alarm
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                startCalendar.timeInMillis,
                startPendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context, alarm: StudyAlarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val startIntent = Intent(context, AlarmReceiver::class.java)
        val startPending = PendingIntent.getBroadcast(
            context, alarm.id * 10, startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val endIntent = Intent(context, AlarmReceiver::class.java)
        val endPending = PendingIntent.getBroadcast(
            context, alarm.id * 10 + 1, endIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(startPending)
        alarmManager.cancel(endPending)
    }

    private fun getNextAlarmTime(hour: Int, minute: Int, daysOfWeek: String): Calendar {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val enabledDays = daysOfWeek.split(",").map { it.trim().toInt() }

        // If time has passed today, move to next enabled day
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Find next enabled day
        var attempts = 0
        while (attempts < 7) {
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            // Convert Calendar.DAY_OF_WEEK (1=Sun) to our format (1=Mon)
            val ourDay = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
            if (enabledDays.contains(ourDay)) {
                break
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            attempts++
        }

        return calendar
    }

    fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format("%d:%02d %s", displayHour, minute, amPm)
    }
}
