package com.raushan.upscagent.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.raushan.upscagent.R
import com.raushan.upscagent.ui.alarm.AlarmFragment
import com.raushan.upscagent.ui.agent.AgentFragment
import com.raushan.upscagent.ui.currentaffairs.CurrentAffairsFragment
import com.raushan.upscagent.ui.quiz.QuizFragment
import com.raushan.upscagent.ui.reader.DocumentListFragment

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions()
        setupBottomNavigation()

        // Handle navigation from notification
        val navigateTo = intent.getStringExtra("navigate_to")
        if (navigateTo == "current_affairs") {
            loadFragment(CurrentAffairsFragment())
            findViewById<BottomNavigationView>(R.id.bottom_nav).selectedItemId = R.id.nav_current_affairs
        } else {
            if (savedInstanceState == null) {
                loadFragment(HomeFragment())
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_alarm -> loadFragment(AlarmFragment())
                R.id.nav_quiz -> loadFragment(QuizFragment())
                R.id.nav_current_affairs -> loadFragment(CurrentAffairsFragment())
                R.id.nav_agent -> loadFragment(AgentFragment())
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.SCHEDULE_EXACT_ALARM)
            }
        }
        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }
}
