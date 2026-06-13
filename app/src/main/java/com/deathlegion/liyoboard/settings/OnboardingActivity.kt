package com.deathlegion.liyoboard.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.deathlegion.liyoboard.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * OnboardingActivity - First-time setup wizard
 * Guides users through enabling the keyboard
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var btnNext: Button
    private lateinit var btnSkip: Button
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)
        btnNext = findViewById(R.id.btn_next)
        btnSkip = findViewById(R.id.btn_skip)

        val pages = listOf(
            OnboardingPage(
                title = "Welcome to LiyoBoard",
                subtitle = "by Death Legion Team",
                description = "An open-source, privacy-first keyboard that puts you in control. No data ever leaves your device.",
                icon = R.drawable.ic_launcher_foreground
            ),
            OnboardingPage(
                title = "Multilingual",
                subtitle = "Sinhala, English & Tamil",
                description = "Full keyboard support for Sinhala (සිංහල), English, and Tamil (தமிழ்) with seamless switching.",
                icon = R.drawable.ic_language
            ),
            OnboardingPage(
                title = "500+ Fonts",
                subtitle = "Make it yours",
                description = "Choose from over 500 custom fonts across 30 categories including Sinhala and Tamil scripts.",
                icon = R.drawable.ic_fonts
            ),
            OnboardingPage(
                title = "Advanced Theming",
                subtitle = "Express yourself",
                description = "15 built-in themes plus a full theme editor. Customize every color, shape, and animation.",
                icon = R.drawable.ic_palette
            ),
            OnboardingPage(
                title = "Privacy First",
                subtitle = "No internet. No tracking.",
                description = "LiyoBoard has NO internet permission. Your typing data never leaves your device. Period.",
                icon = R.drawable.ic_shield
            ),
            OnboardingPage(
                title = "Enable LiyoBoard",
                subtitle = "Almost there!",
                description = "Tap below to enable LiyoBoard in your system settings, then set it as your default keyboard.",
                icon = R.drawable.ic_keyboard
            )
        )

        viewPager.adapter = OnboardingPagerAdapter(pages)
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        btnNext.setOnClickListener {
            if (currentPage < pages.size - 1) {
                currentPage++
                viewPager.currentItem = currentPage
            } else {
                // Last page - enable keyboard
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                markOnboardingComplete()
                finish()
            }
        }

        btnSkip.setOnClickListener {
            markOnboardingComplete()
            finish()
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPage = position
                btnNext.text = if (position == pages.size - 1) "Enable Keyboard" else "Next"
                btnSkip.visibility = if (position == pages.size - 1) View.GONE else View.VISIBLE
            }
        })
    }

    private fun markOnboardingComplete() {
        val prefs = getSharedPreferences("liyoboard_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_complete", true).apply()
    }

    data class OnboardingPage(
        val title: String,
        val subtitle: String,
        val description: String,
        val icon: Int
    )

    inner class OnboardingPagerAdapter(private val pages: List<OnboardingPage>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<OnboardingViewHolder>() {

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): OnboardingViewHolder {
            val view = layoutInflater.inflate(R.layout.item_onboarding, parent, false)
            return OnboardingViewHolder(view)
        }

        override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
            val page = pages[position]
            holder.tvTitle.text = page.title
            holder.tvSubtitle.text = page.subtitle
            holder.tvDescription.text = page.description
            holder.ivIcon.setImageResource(page.icon)
        }

        override fun getItemCount(): Int = pages.size
    }

    inner class OnboardingViewHolder(view: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_onboarding_title)
        val tvSubtitle: TextView = view.findViewById(R.id.tv_onboarding_subtitle)
        val tvDescription: TextView = view.findViewById(R.id.tv_onboarding_description)
        val ivIcon: ImageView = view.findViewById(R.id.iv_onboarding_icon)
    }
}
