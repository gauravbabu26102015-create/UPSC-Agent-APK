package com.raushan.upscagent

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.*
import com.raushan.upscagent.worker.CurrentAffairsWorker
import java.util.concurrent.TimeUnit

class UPSCAgentApp : Application() {

    companion object {
        const val CHANNEL_ALARM = "alarm_channel"
        const val CHANNEL_STUDY = "study_channel"
        const val CHANNEL_CURRENT_AFFAIRS = "current_affairs_channel"
        const val CHANNEL_MOTIVATION = "motivation_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleCurrentAffairsWorker()
        scheduleDailyMotivation()
    }

    private fun createNotificationChannels() {
        val alarmChannel = NotificationChannel(
            CHANNEL_ALARM,
            "Study Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm notifications for study schedule"
            enableVibration(true)
            setSound(null, null) // We handle sound ourselves
        }

        val studyChannel = NotificationChannel(
            CHANNEL_STUDY,
            "Study Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Study session reminders and tips"
        }

        val currentAffairsChannel = NotificationChannel(
            CHANNEL_CURRENT_AFFAIRS,
            "Current Affairs",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily current affairs notifications"
        }

        val motivationChannel = NotificationChannel(
            CHANNEL_MOTIVATION,
            "Daily Motivation",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily motivational quotes and advice"
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(alarmChannel)
        manager.createNotificationChannel(studyChannel)
        manager.createNotificationChannel(currentAffairsChannel)
        manager.createNotificationChannel(motivationChannel)
    }

    private fun scheduleCurrentAffairsWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<CurrentAffairsWorker>(
            6, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "current_affairs_fetch",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun scheduleDailyMotivation() {
        val workRequest = PeriodicWorkRequestBuilder<CurrentAffairsWorker>(
            12, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_motivation",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
