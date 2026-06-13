package com.deathlegion.liyoboard.theme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.deathlegion.liyoboard.databinding.ActivityThemePreviewBinding

/**
 * ThemePreviewActivity - Preview a theme before applying
 */
class ThemePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityThemePreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val themeJson = intent.getStringExtra("theme_json")
        themeJson?.let {
            val theme = LiyoTheme.fromJson(it)
            applyPreviewTheme(theme)
        }
    }

    private fun applyPreviewTheme(theme: LiyoTheme) {
        binding.apply {
            previewContainer.setBackgroundColor(theme.keyboardBgColor)
            previewKey1.setBackgroundColor(theme.keyBgColor)
            previewKey1.setTextColor(theme.keyTextColor)
            previewKey2.setBackgroundColor(theme.keyBgColor)
            previewKey2.setTextColor(theme.keyTextColor)
            previewSpecialKey.setBackgroundColor(theme.specialKeyBgColor)
            previewSpecialKey.setTextColor(theme.specialKeyTextColor)
            previewSpaceBar.setBackgroundColor(theme.keyBgColor)
            previewEnterKey.setBackgroundColor(theme.accentColor)

            tvThemeName.text = theme.name
            tvThemeAuthor.text = "by ${theme.author}"
        }
    }
}
