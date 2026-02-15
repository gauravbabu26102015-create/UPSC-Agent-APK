package com.raushan.upscagent.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.raushan.upscagent.R
import com.raushan.upscagent.UPSCAgentApp
import com.raushan.upscagent.data.repository.AppRepository
import com.raushan.upscagent.ui.home.MainActivity
import com.raushan.upscagent.utils.CurrentAffairsFetcher
import com.raushan.upscagent.utils.MotivationHelper

class CurrentAffairsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val repository = AppRepository(applicationContext)

            // Fetch current affairs
            val affairs = CurrentAffairsFetcher.fetchFromMultipleSources()
            if (affairs.isNotEmpty()) {
                repository.insertAffairs(affairs)
                sendCurrentAffairsNotification(affairs.size)
            }

            // Send motivational notification
            sendMotivationNotification()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun sendCurrentAffairsNotification(count: Int) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra("navigate_to", "current_affairs")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, UPSCAgentApp.CHANNEL_CURRENT_AFFAIRS)
            .setContentTitle("📰 Current Affairs Updated!")
            .setContentText("$count new articles ready. Read before your study session!")
            .setSmallIcon(R.drawable.ic_newspaper)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$count new current affairs articles have been collected from multiple sources. Stay updated for your UPSC preparation!"))
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, notification)
    }

    private fun sendMotivationNotification() {
        val quote = MotivationHelper.getDailyQuote()
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, UPSCAgentApp.CHANNEL_MOTIVATION)
            .setContentTitle("💪 Daily Motivation")
            .setContentText(quote.quote)
            .setSmallIcon(R.drawable.ic_motivation)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("\"${quote.quote}\" — ${quote.author}"))
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1002, notification)
    }
}
