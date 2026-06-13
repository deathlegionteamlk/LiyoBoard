package com.deathlegion.liyoboard.theme

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * ThemeManager - Manages all keyboard themes
 * Built-in themes + custom themes + imported themes
 * Everything stored locally - NO server calls
 */
class ThemeManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("liyoboard_themes", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val themesDir = File(context.filesDir, "themes")
    private var themes = mutableListOf<LiyoTheme>()
    private var activeThemeId: String = "catppuccin_mocha"

    companion object {
        @Volatile
        private var instance: ThemeManager? = null

        fun initialize(context: Context) {
            instance = ThemeManager(context.applicationContext)
            instance?.loadThemes()
        }

        fun getInstance(context: Context): ThemeManager {
            return instance ?: ThemeManager(context.applicationContext).also {
                instance = it
                it.loadThemes()
            }
        }

        fun getDefaultTheme(): LiyoTheme = builtInThemes().first()
    }

    init {
        themesDir.mkdirs()
    }

    // ==========================================
    // BUILT-IN THEMES
    // ==========================================

    private fun loadThemes() {
        themes.clear()
        themes.addAll(builtInThemes())

        // Load custom themes
        val customThemesJson = prefs.getString("custom_themes", null)
        customThemesJson?.let {
            val type = object : TypeToken<List<LiyoTheme>>() {}.type
            val customThemes: List<LiyoTheme> = gson.fromJson(it, type)
            themes.addAll(customThemes)
        }

        // Load imported theme files
        themesDir.listFiles()?.filter { it.extension == "json" }?.forEach { file ->
            try {
                val theme = LiyoTheme.fromJson(file.readText())
                if (!themes.any { it.id == theme.id }) {
                    themes.add(theme)
                }
            } catch (e: Exception) {
                // Skip invalid theme files
            }
        }

        activeThemeId = prefs.getString("active_theme_id", "catppuccin_mocha") ?: "catppuccin_mocha"
    }

    fun getActiveTheme(): LiyoTheme {
        return themes.find { it.id == activeThemeId } ?: builtInThemes().first()
    }

    fun setActiveTheme(themeId: String) {
        activeThemeId = themeId
        prefs.edit().putString("active_theme_id", themeId).apply()
    }

    fun getAllThemes(): List<LiyoTheme> = themes.toList()

    fun getThemeById(id: String): LiyoTheme? = themes.find { it.id == id }

    fun saveCustomTheme(theme: LiyoTheme) {
        themes.removeIf { it.id == theme.id }
        themes.add(theme)

        val customThemes = themes.filter { !builtInThemes().any { bt -> bt.id == it.id } }
        prefs.edit().putString("custom_themes", gson.toJson(customThemes)).apply()
    }

    fun deleteCustomTheme(themeId: String) {
        val theme = themes.find { it.id == themeId }
        if (theme != null && !builtInThemes().any { it.id == themeId }) {
            themes.remove(theme)
            val customThemes = themes.filter { !builtInThemes().any { bt -> bt.id == it.id } }
            prefs.edit().putString("custom_themes", gson.toJson(customThemes)).apply()
        }
    }

    fun exportTheme(themeId: String): File? {
        val theme = themes.find { it.id == themeId } ?: return null
        val file = File(themesDir, "${theme.id}.json")
        file.writeText(theme.toJson())
        return file
    }

    fun importTheme(jsonString: String): LiyoTheme? {
        return try {
            val theme = LiyoTheme.fromJson(jsonString)
            saveCustomTheme(theme)
            // Save to file
            val file = File(themesDir, "${theme.id}.json")
            file.writeText(jsonString)
            theme
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        fun builtInThemes(): List<LiyoTheme> = listOf(
            // Catppuccin Mocha (Default)
            LiyoTheme(
                id = "catppuccin_mocha",
                name = "Catppuccin Mocha",
                author = "Death Legion",
                isDark = true,
                keyboardBgColor = Color.parseColor("#1E1E2E"),
                keyBgColor = Color.parseColor("#313244"),
                keyPressedBgColor = Color.parseColor("#45475A"),
                specialKeyBgColor = Color.parseColor("#45475A"),
                keyTextColor = Color.parseColor("#CDD6F4"),
                specialKeyTextColor = Color.parseColor("#A6ADC8"),
                accentColor = Color.parseColor("#CBA6F7"),
                keyCornerRadius = 12f,
                keyStyle = LiyoTheme.KeyStyle.ROUNDED
            ),
            // Dracula
            LiyoTheme(
                id = "dracula",
                name = "Dracula",
                author = "Death Legion",
                isDark = true,
                keyboardBgColor = Color.parseColor("#282A36"),
                keyBgColor = Color.parseColor("#44475A"),
                keyPressedBgColor = Color.parseColor("#6272A4"),
                specialKeyBgColor = Color.parseColor("#6272A4"),
                keyTextColor = Color.parseColor("#F8F8F2"),
                specialKeyTextColor = Color.parseColor("#BD93F9"),
                accentColor = Color.parseColor("#BD93F9"),
                keyCornerRadius = 8f,
                keyStyle = LiyoTheme.KeyStyle.MATERIAL
            ),
            // Nord
            LiyoTheme(
                id = "nord",
                name = "Nord",
                author = "Death Legion",
                isDark = true,
                keyboardBgColor = Color.parseColor("#2E3440"),
                keyBgColor = Color.parseColor("#3B4252"),
                keyPressedBgColor = Color.parseColor("#434C5E"),
                specialKeyBgColor = Color.parseColor("#434C5E"),
                keyTextColor = Color.parseColor("#D8DEE9"),
                specialKeyTextColor = Color.parseColor("#81A1C1"),
                accentColor = Color.parseColor("#88C0D0"),
                keyCornerRadius = 10f,
                keyStyle = LiyoTheme.KeyStyle.ROUNDED
            ),
            // Tokyo Night
            LiyoTheme(
                id = "tokyo_night",
                name = "Tokyo Night",
                author = "Death Legion",
                isDark = true,
                keyboardBgColor = Color.parseColor("#1A1B26"),
                keyBgColor = Color.parseColor("#24283B"),
                keyPressedBgColor = Color.parseColor("#414868"),
                specialKeyBgColor = Color.parseColor("#414868"),
                keyTextColor = Color.parseColor("#C0CAF5"),
                specialKeyTextColor = Color.parseColor("#7AA2F7"),
                accentColor = Color.parseColor("#7AA2F7"),
                keyCornerRadius = 14f,
                keyStyle = LiyoTheme.KeyStyle.PILL
            ),
            // Solarized Dark
            LiyoTheme(
                id = "solarized_dark",
                name = "Solarized Dark",
                author = "Death Legion",
                isDark = true,
                keyboardBgColor = Color.parseColor("#002B36"),
                keyBgColor = Color.parseColor("#073642"),
                keyPressedBgColor = Color.parseColor("#0A4050"),
                specialKeyBgColor = Color.parseColor("#0A4050"),
                keyTextColor = Color.parseColor("#839496"),
                specialKeyTextColor = Color.parseColor("#268BD2"),
                accentColor = Color.parseColor("#268BD2"),
                keyCornerRadius = 4f,
                keyStyle = LiyoTheme.KeyStyle.SHARP
            ),
            // Rose Pine
            LiyoTheme(
                id = "rose_pine",
                name = "Rosé Pine",
                author = "Death Legion",
                isDark = true,
                keyboardBgColor = Color.parseColor("#191724"),
                keyBgColor = Color.parseColor("#1F1D2E"),
                keyPressedBgColor = Color.parseColor("#26233A"),
                specialKeyBgColor = Color.parseColor("#26233A"),
                keyTextColor = Color.parseColor("#E0DEF4"),
                specialKeyTextColor = Color.parseColor("#C4A7E7"),
                accentColor = Color.parseColor("#EB6F92"),
                keyCornerRadius = 12f,
                keyStyle = LiyoTheme.KeyStyle.ROUNDED
            ),
            // Gruvbox Dark
            LiyoTheme(
                id = "gruvbox_dark",
                name = "Gruvbox Dark",
                author = "Death Legion",
                isDark = true,
                keyboardBgColor = Color.parseColor("#282828"),
                keyBgColor = Color.parseColor("#3C3836"),
                keyPressedBgColor = Color.parseColor("#504945"),
                specialKeyBgColor = Color.parseColor("#504945"),
                keyTextColor = Color.parseColor("#EBDBB2"),
                specialKeyTextColor = Color.parseColor("#FE8019"),
                accentColor = Color.parseColor("#FE8019"),
                keyCornerRadius = 6f,
                keyStyle = LiyoTheme.KeyStyle.MINIMAL
            ),
            // Material Light
            LiyoTheme(
                id = "material_light",
                name = "Material Light",
                author = "Death Legion",
                isDark = false,
                keyboardBgColor = Color.parseColor("#F5F5F5"),
                keyBgColor = Color.parseColor("#FFFFFF"),
                keyPressedBgColor = Color.parseColor("#E0E0E0"),
                specialKeyBgColor = Color.parseColor("#E0E0E0"),
                keyTextColor = Color.parseColor("#212121"),
                specialKeyTextColor = Color.parseColor("#757575"),
                accentColor = Color.parseColor("#6200EE"),
                keyCornerRadius = 8f,
                keyStyle = LiyoTheme.KeyStyle.MATERIAL
            ),
            // iOS Style
            LiyoTheme(
                id = "ios_style",
                name = "iOS Style",
                author = "Death Legion",
                isDark = false,
                keyboardBgColor = Color.parseColor("#D1D3D9"),
                keyBgColor = Color.parseColor("#FFFFFF"),
                keyPressedBgColor = Color.parseColor("#E8E8ED"),
                specialKeyBgColor = Color.parseColor("#A8A8AD"),
                keyTextColor = Color.parseColor("#000000"),
                specialKeyTextColor = Color.parseColor("#000000"),
                accentColor = Color.parseColor("#007AFF"),
                keyCornerRadius = 6f,
                keyStyle = LiyoTheme.KeyStyle.IOS
            ),
            // AMOLED Black
            LiyoTheme(
                id = "amoled_black",
                name = "AMOLED Black",
                author = "Death Legion",
                isDark = true,
                keyboardBgColor = Color.parseColor("#000000"),
                keyBgColor = Color.parseColor("#0A0A0A"),
                keyPressedBgColor = Color.parseColor("#1A1A1A"),
                specialKeyBgColor = Color.parseColor("#1A1A1A"),
                keyTextColor = Color.parseColor("#FFFFFF"),
                specialKeyTextColor = Color.parseColor("#666666"),
                accentColor = Color.parseColor("#4FC3F7"),
                keyCornerRadius = 8f,
                keyStyle = LiyoTheme.KeyStyle.MINIMAL
            ),
            // Neon Cyberpunk
            LiyoTheme(
                id = "neon_cyberpunk",
                name = "Neon Cyberpunk",
                author = "Death Legion",
                isDark = true,
                keyboardBgColor = Color.parseColor("#0D0221"),
                keyBgColor = Color.parseColor("#150734"),
                keyPressedBgColor = Color.parseColor("#1F0E4E"),
                specialKeyBgColor = Color.parseColor("#1F0E4E"),
                keyTextColor = Color.parseColor("#00FF9F"),
                specialKeyTextColor = Color.parseColor("#FF00FF"),
                accentColor = Color.parseColor("#00FFFF"),
                keyCornerRadius = 4f,
                keyStyle = LiyoTheme.KeyStyle.SHARP
            ),
            // Ocean Wave
            LiyoTheme(
                id = "ocean_wave",
                name = "Ocean Wave",
                author = "Death Legion",
                isDark = true,
                keyboardBgColor = Color.parseColor("#0B1622"),
                keyBgColor = Color.parseColor("#162638"),
                keyPressedBgColor = Color.parseColor("#1D3A54"),
                specialKeyBgColor = Color.parseColor("#1D3A54"),
                keyTextColor = Color.parseColor("#A8D8EA"),
                specialKeyTextColor = Color.parseColor("#AA96DA"),
                accentColor = Color.parseColor("#00B4D8"),
                keyCornerRadius = 16f,
                keyStyle = LiyoTheme.KeyStyle.PILL
            ),
            // Neumorphic
            LiyoTheme(
                id = "neumorphic",
                name = "Neumorphic",
                author = "Death Legion",
                isDark = true,
                keyboardBgColor = Color.parseColor("#2D2D2D"),
                keyBgColor = Color.parseColor("#333333"),
                keyPressedBgColor = Color.parseColor("#2A2A2A"),
                specialKeyBgColor = Color.parseColor("#2A2A2A"),
                keyTextColor = Color.parseColor("#CCCCCC"),
                specialKeyTextColor = Color.parseColor("#999999"),
                accentColor = Color.parseColor("#6C63FF"),
                keyCornerRadius = 16f,
                keyStyle = LiyoTheme.KeyStyle.NEUMORPHIC
            ),
            // Cherry Blossom
            LiyoTheme(
                id = "cherry_blossom",
                name = "Cherry Blossom",
                author = "Death Legion",
                isDark = false,
                keyboardBgColor = Color.parseColor("#FFF0F5"),
                keyBgColor = Color.parseColor("#FFE4EC"),
                keyPressedBgColor = Color.parseColor("#FFCCD5"),
                specialKeyBgColor = Color.parseColor("#FFB6C1"),
                keyTextColor = Color.parseColor("#8B2252"),
                specialKeyTextColor = Color.parseColor("#CD6090"),
                accentColor = Color.parseColor("#FF1493"),
                keyCornerRadius = 20f,
                keyStyle = LiyoTheme.KeyStyle.PILL
            ),
            // Emerald
            LiyoTheme(
                id = "emerald",
                name = "Emerald",
                author = "Death Legion",
                isDark = true,
                keyboardBgColor = Color.parseColor("#0B1D0E"),
                keyBgColor = Color.parseColor("#142B18"),
                keyPressedBgColor = Color.parseColor("#1E3D23"),
                specialKeyBgColor = Color.parseColor("#1E3D23"),
                keyTextColor = Color.parseColor("#A8E6CF"),
                specialKeyTextColor = Color.parseColor("#55B88D"),
                accentColor = Color.parseColor("#2ECC71"),
                keyCornerRadius = 10f,
                keyStyle = LiyoTheme.KeyStyle.ROUNDED
            )
        )
    }
}
