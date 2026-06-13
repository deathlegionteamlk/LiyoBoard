package com.deathlegion.liyoboard.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.deathlegion.liyoboard.R
import com.deathlegion.liyoboard.databinding.ActivityThemeEditorBinding
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

/**
 * ThemeEditorActivity - Full visual theme editor
 * Users can customize every aspect of the keyboard appearance
 */
class ThemeEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityThemeEditorBinding
    private lateinit var themeManager: ThemeManager
    private var currentTheme: LiyoTheme? = null
    private var isNewTheme = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemeEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        themeManager = ThemeManager.getInstance(this)

        val themeId = intent.getStringExtra("theme_id")
        if (themeId != null) {
            currentTheme = themeManager.getThemeById(themeId)
        } else {
            isNewTheme = true
            currentTheme = LiyoTheme(
                id = "custom_${System.currentTimeMillis()}",
                name = "My Theme",
                author = "You"
            )
        }

        setupViews()
        loadThemeIntoEditor()
    }

    private fun setupViews() {
        binding.apply {
            // Theme name
            etThemeName.setText(currentTheme?.name ?: "My Theme")

            // Color pickers
            btnKeyboardBgColor.setOnClickListener { showColorPicker("Keyboard Background", currentTheme?.keyboardBgColor ?: 0) { color -> currentTheme = currentTheme?.copy(keyboardBgColor = color) } }
            btnKeyBgColor.setOnClickListener { showColorPicker("Key Background", currentTheme?.keyBgColor ?: 0) { color -> currentTheme = currentTheme?.copy(keyBgColor = color) } }
            btnKeyPressedColor.setOnClickListener { showColorPicker("Key Pressed", currentTheme?.keyPressedBgColor ?: 0) { color -> currentTheme = currentTheme?.copy(keyPressedBgColor = color) } }
            btnSpecialKeyColor.setOnClickListener { showColorPicker("Special Key", currentTheme?.specialKeyBgColor ?: 0) { color -> currentTheme = currentTheme?.copy(specialKeyBgColor = color) } }
            btnKeyTextColor.setOnClickListener { showColorPicker("Key Text", currentTheme?.keyTextColor ?: 0) { color -> currentTheme = currentTheme?.copy(keyTextColor = color) } }
            btnAccentColor.setOnClickListener { showColorPicker("Accent", currentTheme?.accentColor ?: 0) { color -> currentTheme = currentTheme?.copy(accentColor = color) } }

            // Corner radius
            seekCornerRadius.progress = (currentTheme?.keyCornerRadius?.toInt() ?: 12)
            seekCornerRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    currentTheme = currentTheme?.copy(keyCornerRadius = progress.toFloat())
                    tvCornerRadiusValue.text = "${progress}dp"
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            // Key style
            spinnerKeyStyle.setSelection(currentTheme?.keyStyle?.ordinal ?: 0)

            // Dark mode toggle
            switchDarkMode.isChecked = currentTheme?.isDark ?: false
            switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
                currentTheme = currentTheme?.copy(isDark = isChecked)
            }

            // Font selector
            btnSelectFont.setOnClickListener {
                // Open font browser
                val intent = android.content.Intent(this@ThemeEditorActivity, com.deathlegion.liyoboard.fonts.FontBrowserActivity::class.java)
                startActivity(intent)
            }

            // Save button
            btnSaveTheme.setOnClickListener {
                saveTheme()
            }

            // Preview button
            btnPreviewTheme.setOnClickListener {
                val intent = android.content.Intent(this@ThemeEditorActivity, ThemePreviewActivity::class.java)
                intent.putExtra("theme_json", currentTheme?.toJson())
                startActivity(intent)
            }

            // Export button
            btnExportTheme.setOnClickListener {
                exportTheme()
            }
        }
    }

    private fun loadThemeIntoEditor() {
        currentTheme?.let { theme ->
            binding.etThemeName.setText(theme.name)
            binding.seekCornerRadius.progress = theme.keyCornerRadius.toInt()
            binding.tvCornerRadiusValue.text = "${theme.keyCornerRadius.toInt()}dp"
            binding.switchDarkMode.isChecked = theme.isDark
        }
    }

    private fun showColorPicker(title: String, currentColor: Int, onColorSelected: (Int) -> Unit) {
        ColorPickerDialog.Builder(this)
            .setTitle(title)
            .setPreferenceName("LiyoBoardColorPicker")
            .setPositiveButton("Apply", object : ColorEnvelopeListener {
                override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                    envelope?.let {
                        onColorSelected(it.color)
                        updatePreview()
                    }
                }
            })
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .show()
    }

    private fun updatePreview() {
        // Refresh the preview area with current theme colors
        currentTheme?.let { theme ->
            binding.previewKeyboardView.setBackgroundColor(theme.keyboardBgColor)
        }
    }

    private fun saveTheme() {
        val name = binding.etThemeName.text.toString().ifBlank { "Unnamed Theme" }
        currentTheme = currentTheme?.copy(name = name)

        currentTheme?.let { theme ->
            themeManager.saveCustomTheme(theme)
            Toast.makeText(this, "Theme saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun exportTheme() {
        currentTheme?.let { theme ->
            val file = themeManager.exportTheme(theme.id)
            if (file != null) {
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND)
                shareIntent.type = "application/json"
                shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    file
                ))
                shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(android.content.Intent.createChooser(shareIntent, "Share Theme"))
            }
        }
    }
}
