package com.raushan.upscagent.ui.alarm

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.raushan.upscagent.R
import com.raushan.upscagent.service.AlarmService
import com.raushan.upscagent.utils.MotivationHelper

class AlarmRingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show on lock screen
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        setContentView(R.layout.activity_alarm_ring)

        val subject = intent.getStringExtra("subject") ?: "Study Time"
        val message = intent.getStringExtra("message") ?: "Time to study!"
        val type = intent.getStringExtra("type") ?: "start"

        findViewById<TextView>(R.id.tv_alarm_subject_ring).text = subject
        findViewById<TextView>(R.id.tv_alarm_message).text = message
        findViewById<TextView>(R.id.tv_alarm_type).text = if (type == "start") "📚 Session Starting!" else "✅ Session Complete!"
        findViewById<TextView>(R.id.tv_alarm_motivation).text = MotivationHelper.getRandomQuote().quote

        findViewById<Button>(R.id.btn_dismiss_alarm).setOnClickListener {
            stopAlarmService()
            finish()
        }

        findViewById<Button>(R.id.btn_snooze_alarm).setOnClickListener {
            stopAlarmService()
            // TODO: Implement snooze (reschedule alarm for 5 minutes later)
            finish()
        }
    }

    private fun stopAlarmService() {
        val serviceIntent = Intent(this, AlarmService::class.java)
        stopService(serviceIntent)
    }

    override fun onBackPressed() {
        // Don't allow back press - must dismiss or snooze
    }
}
