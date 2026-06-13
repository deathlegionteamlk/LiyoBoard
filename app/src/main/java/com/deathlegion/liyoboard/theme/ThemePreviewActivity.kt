package com.deathlegion.liyoboard.theme

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.deathlegion.liyoboard.R

class ThemePreviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_preview)
        val themeJson = intent.getStringExtra("theme_json")
        themeJson?.let {
            val theme = LiyoTheme.fromJson(it)
            findViewById<TextView>(R.id.tv_theme_name)?.setText(theme.name)
            findViewById<TextView>(R.id.tv_theme_author)?.setText("by ${theme.author}")
            findViewById<Button>(R.id.preview_key_1)?.setBackgroundColor(theme.keyBgColor)
            findViewById<Button>(R.id.preview_key_2)?.setBackgroundColor(theme.keyBgColor)
            findViewById<Button>(R.id.preview_special_key)?.setBackgroundColor(theme.specialKeyBgColor)
            findViewById<Button>(R.id.preview_space_bar)?.setBackgroundColor(theme.keyBgColor)
            findViewById<Button>(R.id.preview_enter_key)?.setBackgroundColor(theme.accentColor)
        }
    }
}
