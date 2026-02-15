package com.raushan.upscagent.ui.currentaffairs

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.raushan.upscagent.R

class CurrentAffairsDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_affairs_detail)

        val title = intent.getStringExtra("title") ?: ""
        val content = intent.getStringExtra("content") ?: ""
        val source = intent.getStringExtra("source") ?: ""

        findViewById<TextView>(R.id.tv_detail_title).text = title
        findViewById<TextView>(R.id.tv_detail_content).text = content
        findViewById<TextView>(R.id.tv_detail_source).text = source
        findViewById<ImageButton>(R.id.btn_back_detail).setOnClickListener { finish() }
    }
}
