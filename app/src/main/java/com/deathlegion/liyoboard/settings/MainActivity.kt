package com.deathlegion.liyoboard.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deathlegion.liyoboard.R
import com.deathlegion.liyoboard.clipboard.ClipboardManagerActivity
import com.deathlegion.liyoboard.fonts.FontBrowserActivity
import com.deathlegion.liyoboard.store.ExtensionStoreActivity
import com.deathlegion.liyoboard.theme.ThemeEditorActivity
import com.deathlegion.liyoboard.theme.ThemeManager
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * MainActivity - Main settings and hub for LiyoBoard
 * Shows setup status, quick settings, and navigation to all features
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if keyboard is enabled
        checkKeyboardStatus()

        setupNavigation()
        setupQuickSettings()
    }

    override fun onResume() {
        super.onResume()
        checkKeyboardStatus()
    }

    private fun checkKeyboardStatus() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        val enabledInputMethods = imm.enabledInputMethodList
        val isLiyoBoardEnabled = enabledInputMethods.any {
            it.packageName == packageName
        }

        val isDefault = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        ) == "${packageName}/com.deathlegion.liyoboard.keyboard.LiyoBoardIME"

        val statusCard = findViewById<View>(R.id.card_setup_status)
        val tvStatus = statusCard.findViewById<TextView>(R.id.tv_setup_status)
        val tvStatusDetail = statusCard.findViewById<TextView>(R.id.tv_setup_detail)
        val btnSetup = statusCard.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_setup_action)

        if (!isLiyoBoardEnabled) {
            tvStatus.text = "Step 1: Enable LiyoBoard"
            tvStatusDetail.text = "LiyoBoard needs to be enabled in your system settings to work as your keyboard."
            btnSetup.text = "Enable Keyboard"
            btnSetup.setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
            statusCard.visibility = View.VISIBLE
        } else if (!isDefault) {
            tvStatus.text = "Step 2: Set as Default"
            tvStatusDetail.text = "LiyoBoard is enabled but not set as your default keyboard. Tap below to switch."
            btnSetup.text = "Set as Default"
            btnSetup.setOnClickListener {
                imm.showInputMethodPicker()
            }
            statusCard.visibility = View.VISIBLE
        } else {
            tvStatus.text = "LiyoBoard is Active!"
            tvStatusDetail.text = "Your privacy-first keyboard is ready. Explore settings below."
            btnSetup.visibility = View.GONE
        }
    }

    private fun setupNavigation() {
        // Theme editor
        findViewById<View>(R.id.nav_themes).setOnClickListener {
            startActivity(Intent(this, ThemeEditorActivity::class.java))
        }

        // Font browser
        findViewById<View>(R.id.nav_fonts).setOnClickListener {
            startActivity(Intent(this, FontBrowserActivity::class.java))
        }

        // Clipboard manager
        findViewById<View>(R.id.nav_clipboard).setOnClickListener {
            startActivity(Intent(this, ClipboardManagerActivity::class.java))
        }

        // Extension store
        findViewById<View>(R.id.nav_extensions).setOnClickListener {
            startActivity(Intent(this, ExtensionStoreActivity::class.java))
        }

        // Advanced settings
        findViewById<View>(R.id.nav_advanced).setOnClickListener {
            startActivity(Intent(this, AdvancedSettingsActivity::class.java))
        }

        // About
        findViewById<View>(R.id.nav_about).setOnClickListener {
            showAboutDialog()
        }
    }

    private fun setupQuickSettings() {
        val prefs = getSharedPreferences("liyoboard_prefs", Context.MODE_PRIVATE)

        // Haptic feedback toggle
        val switchHaptic = findViewById<SwitchMaterial>(R.id.switch_haptic)
        switchHaptic.isChecked = prefs.getBoolean("haptic_feedback", true)
        switchHaptic.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("haptic_feedback", isChecked).apply()
        }

        // Sound feedback toggle
        val switchSound = findViewById<SwitchMaterial>(R.id.switch_sound)
        switchSound.isChecked = prefs.getBoolean("sound_feedback", false)
        switchSound.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("sound_feedback", isChecked).apply()
        }

        // Clipboard history toggle
        val switchClipboard = findViewById<SwitchMaterial>(R.id.switch_clipboard_history)
        switchClipboard.isChecked = prefs.getBoolean("clipboard_history_enabled", true)
        switchClipboard.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("clipboard_history_enabled", isChecked).apply()
        }

        // Auto-capitalization
        val switchAutoCap = findViewById<SwitchMaterial>(R.id.switch_auto_cap)
        switchAutoCap.isChecked = prefs.getBoolean("auto_capitalization", true)
        switchAutoCap.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_capitalization", isChecked).apply()
        }
    }

    private fun showAboutDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("About LiyoBoard")
            .setMessage(
                "LiyoBoard v1.0.0-beta1\n\n" +
                "By Death Legion Team\n\n" +
                "An open-source, privacy-first keyboard.\n" +
                "No data ever leaves your device.\n\n" +
                "Features:\n" +
                "- Sinhala, English & Tamil support\n" +
                "- 500+ custom fonts\n" +
                "- Advanced theming engine\n" +
                "- Built-in clipboard manager\n" +
                "- Emoji keyboard with recents\n" +
                "- Extension system\n" +
                "- Zero network access\n\n" +
                "This is a beta release.\n" +
                "Spell check coming soon!"
            )
            .setPositiveButton("Got it", null)
            .show()
    }
}
