package com.deathlegion.liyoboard.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.deathlegion.liyoboard.R

/**
 * AdvancedSettingsActivity - Detailed keyboard configuration
 */
class AdvancedSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_settings)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, AdvancedSettingsFragment())
            .commit()
    }

    class AdvancedSettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            val context = preferenceManager.context
            val screen = preferenceManager.createPreferenceScreen(context)

            // Keyboard behavior
            val behaviorCategory = androidx.preference.PreferenceCategory(context).apply {
                title = "Keyboard Behavior"
            }
            screen.addPreference(behaviorCategory)

            // Key long press delay
            SeekBarPreference(context).apply {
                key = "long_press_delay"
                title = "Long Press Delay"
                summary = "How long to hold a key for alternate characters"
                setDefaultValue(300)
                min = 100
                max = 800
            }.also { behaviorCategory.addPreference(it) }

            // Key repeat delay
            SeekBarPreference(context).apply {
                key = "key_repeat_delay"
                title = "Key Repeat Delay"
                summary = "Delay before key starts repeating"
                setDefaultValue(50)
                min = 10
                max = 200
            }.also { behaviorCategory.addPreference(it) }

            // Vibration duration
            SeekBarPreference(context).apply {
                key = "vibration_duration"
                title = "Vibration Duration"
                summary = "How long the vibration lasts on key press"
                setDefaultValue(20)
                min = 5
                max = 100
            }.also { behaviorCategory.addPreference(it) }

            // Auto-space after punctuation
            androidx.preference.SwitchPreferenceCompat(context).apply {
                key = "auto_space_punctuation"
                title = "Auto-space After Punctuation"
                summary = "Automatically add space after punctuation marks"
                setDefaultValue(true)
            }.also { behaviorCategory.addPreference(it) }

            // Double-space period
            androidx.preference.SwitchPreferenceCompat(context).apply {
                key = "double_space_period"
                title = "Double-space Period"
                summary = "Double tap space to insert a period"
                setDefaultValue(true)
            }.also { behaviorCategory.addPreference(it) }

            // Appearance
            val appearanceCategory = androidx.preference.PreferenceCategory(context).apply {
                title = "Appearance"
            }
            screen.addPreference(appearanceCategory)

            // Keyboard height
            SeekBarPreference(context).apply {
                key = "keyboard_height"
                title = "Keyboard Height"
                summary = "Adjust the height of the keyboard"
                setDefaultValue(100)
                min = 60
                max = 140
            }.also { appearanceCategory.addPreference(it) }

            // Key height
            SeekBarPreference(context).apply {
                key = "key_height_factor"
                title = "Key Height"
                summary = "Adjust individual key height"
                setDefaultValue(100)
                min = 70
                max = 130
            }.also { appearanceCategory.addPreference(it) }

            // Show toolbar
            androidx.preference.SwitchPreferenceCompat(context).apply {
                key = "show_toolbar"
                title = "Show Toolbar"
                summary = "Show the quick-access toolbar above the keyboard"
                setDefaultValue(true)
            }.also { appearanceCategory.addPreference(it) }

            // Show suggestion bar
            androidx.preference.SwitchPreferenceCompat(context).apply {
                key = "show_suggestions"
                title = "Show Suggestion Bar"
                summary = "Show the suggestion bar above the keyboard"
                setDefaultValue(false) // Beta - not yet implemented
            }.also { appearanceCategory.addPreference(it) }

            // Key press animation
            val animOptions = arrayOf("None", "Scale", "Ripple", "Glow")
            androidx.preference.ListPreference(context).apply {
                key = "key_press_animation"
                title = "Key Press Animation"
                summary = "Animation style when pressing keys"
                entries = animOptions
                entryValues = arrayOf("none", "scale", "ripple", "glow")
                setDefaultValue("scale")
            }.also { appearanceCategory.addPreference(it) }

            // Clipboard settings
            val clipboardCategory = androidx.preference.PreferenceCategory(context).apply {
                title = "Clipboard"
            }
            screen.addPreference(clipboardCategory)

            // Clipboard history size
            SeekBarPreference(context).apply {
                key = "clipboard_history_size"
                title = "History Size"
                summary = "Maximum number of clipboard items to keep"
                setDefaultValue(100)
                min = 10
                max = 500
            }.also { clipboardCategory.addPreference(it) }

            // Auto-cleanup days
            SeekBarPreference(context).apply {
                key = "clipboard_cleanup_days"
                title = "Auto-cleanup (Days)"
                summary = "Automatically remove items older than this"
                setDefaultValue(30)
                min = 1
                max = 365
            }.also { clipboardCategory.addPreference(it) }

            // Privacy
            val privacyCategory = androidx.preference.PreferenceCategory(context).apply {
                title = "Privacy & Security"
            }
            screen.addPreference(privacyCategory)

            androidx.preference.Preference(context).apply {
                title = "Privacy Statement"
                summary = "LiyoBoard never connects to the internet. No data leaves your device."
                isSelectable = false
            }.also { privacyCategory.addPreference(it) }

            androidx.preference.Preference(context).apply {
                title = "View Permissions"
                summary = "LiyoBoard only requests VIBRATE permission"
                isSelectable = false
            }.also { privacyCategory.addPreference(it) }

            // Clear all data
            androidx.preference.Preference(context).apply {
                title = "Clear All Data"
                summary = "Delete all settings, themes, and clipboard history"
                setOnPreferenceClickListener {
                    androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Clear All Data?")
                        .setMessage("This will delete all your settings, custom themes, clipboard history, and recent emojis. This cannot be undone.")
                        .setPositiveButton("Clear Everything") { _, _ ->
                            clearAllData()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                    true
                }
            }.also { privacyCategory.addPreference(it) }

            preferenceScreen = screen
        }

        private fun clearAllData() {
            val context = preferenceManager.context
            val prefs = context.getSharedPreferences("liyoboard_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            context.getSharedPreferences("liyoboard_themes", Context.MODE_PRIVATE).edit().clear().apply()
            context.getSharedPreferences("liyoboard_clipboard", Context.MODE_PRIVATE).edit().clear().apply()
            context.getSharedPreferences("liyoboard_emoji", Context.MODE_PRIVATE).edit().clear().apply()
            context.getSharedPreferences("liyoboard_extensions", Context.MODE_PRIVATE).edit().clear().apply()
            context.getSharedPreferences("liyoboard_fonts", Context.MODE_PRIVATE).edit().clear().apply()
        }
    }

    /**
     * Custom SeekBar Preference
     */
    class SeekBarPreference(context: Context) : androidx.preference.Preference(context) {
        var min = 0
        var max = 100
        var value = 50
    }
}
