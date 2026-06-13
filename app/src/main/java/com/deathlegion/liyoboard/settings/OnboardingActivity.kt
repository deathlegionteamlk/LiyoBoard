package com.deathlegion.liyoboard.settings

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.deathlegion.liyoboard.R

class OnboardingActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var btnSkip: Button
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        viewPager = findViewById(R.id.view_pager)
        btnNext = findViewById(R.id.btn_next)
        btnSkip = findViewById(R.id.btn_skip)
        val prefs = getSharedPreferences("liyoboard_prefs", MODE_PRIVATE)
        btnSkip.setOnClickListener { prefs.edit().putBoolean("onboarding_complete", true).apply(); finish() }
        btnNext.setOnClickListener { prefs.edit().putBoolean("onboarding_complete", true).apply(); finish() }
    }
}
