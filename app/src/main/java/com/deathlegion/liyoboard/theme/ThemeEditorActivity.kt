package com.deathlegion.liyoboard.theme

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.deathlegion.liyoboard.R

class ThemeEditorActivity : AppCompatActivity() {
    private lateinit var themeManager: ThemeManager
    private var currentTheme: LiyoTheme? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_editor)
        themeManager = ThemeManager.getInstance(this)
        val themeId = intent.getStringExtra("theme_id")
        if (themeId != null) currentTheme = themeManager.getThemeById(themeId)
        else currentTheme = LiyoTheme(id = "custom_${System.currentTimeMillis()}", name = "My Theme", author = "You")
        currentTheme?.let { theme ->
            findViewById<EditText>(R.id.et_theme_name)?.setText(theme.name)
        }
        findViewById<Button>(R.id.btn_save_theme)?.setOnClickListener { saveTheme() }
        findViewById<Button>(R.id.btn_preview_theme)?.setOnClickListener { previewTheme() }
    }
    private fun saveTheme() {
        val name = findViewById<EditText>(R.id.et_theme_name)?.text?.toString() ?: "My Theme"
        currentTheme = currentTheme?.copy(name = name)
        currentTheme?.let { themeManager.saveCustomTheme(it); Toast.makeText(this, "Theme saved!", Toast.LENGTH_SHORT).show(); finish() }
    }
    private fun previewTheme() {
        Toast.makeText(this, "Preview: ${currentTheme?.name}", Toast.LENGTH_SHORT).show()
    }
}
