package com.raushan.upscagent.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.raushan.upscagent.R
import com.raushan.upscagent.utils.MotivationHelper

class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Greeting
        view.findViewById<TextView>(R.id.tv_greeting).text = MotivationHelper.getGreeting()

        // Daily Quote
        val quote = MotivationHelper.getDailyQuote()
        view.findViewById<TextView>(R.id.tv_quote).text = "\"${quote.quote}\""
        view.findViewById<TextView>(R.id.tv_quote_author).text = "— ${quote.author}"

        // Daily Advice
        view.findViewById<TextView>(R.id.tv_daily_advice).text = MotivationHelper.getDailyAdvice()

        // Study Tip
        view.findViewById<TextView>(R.id.tv_study_tip).text = MotivationHelper.getRandomStudyTip()

        // Quick Action Cards
        view.findViewById<CardView>(R.id.card_alarm).setOnClickListener {
            navigateTo(R.id.nav_alarm)
        }
        view.findViewById<CardView>(R.id.card_quiz).setOnClickListener {
            navigateTo(R.id.nav_quiz)
        }
        view.findViewById<CardView>(R.id.card_current_affairs).setOnClickListener {
            navigateTo(R.id.nav_current_affairs)
        }
        view.findViewById<CardView>(R.id.card_agent).setOnClickListener {
            navigateTo(R.id.nav_agent)
        }

        // Refresh tip button
        view.findViewById<View>(R.id.btn_refresh_tip)?.setOnClickListener {
            view.findViewById<TextView>(R.id.tv_study_tip).text = MotivationHelper.getRandomStudyTip()
        }
    }

    private fun navigateTo(navId: Int) {
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.selectedItemId = navId
    }
}
