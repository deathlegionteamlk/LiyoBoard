package com.deathlegion.liyoboard.theme

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class LiyoTheme(
    val id: String, val name: String, val author: String = "Death Legion",
    val version: Int = 1, val isDark: Boolean = false,
    val keyboardBgColor: Int = Color.parseColor("#1E1E2E"),
    val keyBgColor: Int = Color.parseColor("#2D2D44"),
    val keyPressedBgColor: Int = Color.parseColor("#3D3D5C"),
    val specialKeyBgColor: Int = Color.parseColor("#3D3D5C"),
    val keyTextColor: Int = Color.parseColor("#E0E0E0"),
    val keyPressedTextColor: Int = Color.WHITE,
    val specialKeyTextColor: Int = Color.parseColor("#A0A0C0"),
    val keyCornerRadius: Float = 12f,
    val keyBorderWidth: Float = 0f,
    val keyBorderColor: Int = Color.TRANSPARENT,
    val keyStyle: LiyoTheme.KeyStyle = LiyoTheme.KeyStyle.ROUNDED,
    val keyboardFontName: String? = null,
    val keyTextSize: Float = 18f,
    val keyPressAnimation: LiyoTheme.KeyPressAnimation = LiyoTheme.KeyPressAnimation.SCALE,
    val accentColor: Int = Color.parseColor("#7C3AED"),
    val toolbarBgColor: Int = Color.parseColor("#1A1A2E"),
    val toolbarIconColor: Int = Color.parseColor("#A0A0C0"),
    val emojiBgColor: Int = Color.parseColor("#1E1E2E"),
    val emojiCategoryBgColor: Int = Color.parseColor("#2D2D44"),
    val clipboardBgColor: Int = Color.parseColor("#1E1E2E"),
    val clipboardItemBgColor: Int = Color.parseColor("#2D2D44"),
    val suggestionBarBgColor: Int = Color.parseColor("#1A1A2E"),
    val isPremium: Boolean = false
) {
    enum class KeyPressAnimation { NONE, SCALE, RIPPLE, GLOW }
    enum class KeyStyle { ROUNDED, SHARP, CIRCLE, PILL, MINIMAL, MATERIAL, IOS, NEUMORPHIC }
    fun toJson(): String = Gson().toJson(this)
    companion object {
        fun fromJson(json: String): LiyoTheme = Gson().fromJson(json, LiyoTheme::class.java)
    }
}

class ThemeManager private constructor(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("liyoboard_themes", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val themesDir = File(context.filesDir, "themes")
    private var themes = mutableListOf<LiyoTheme>()
    private var activeThemeId: String = "catppuccin_mocha"

    companion object {
        @Volatile private var instance: ThemeManager? = null
        fun initialize(context: Context) { instance = ThemeManager(context.applicationContext); instance?.loadThemes() }
        fun getInstance(context: Context): ThemeManager = instance ?: ThemeManager(context.applicationContext).also { instance = it; it.loadThemes() }
        fun getDefaultTheme(): LiyoTheme = builtInThemes().first()
        fun builtInThemes(): List<LiyoTheme> = listOf(
            LiyoTheme(id="catppuccin_mocha", name="Catppuccin Mocha", author="Death Legion", isDark=true, keyboardBgColor=Color.parseColor("#1E1E2E"), keyBgColor=Color.parseColor("#313244"), keyPressedBgColor=Color.parseColor("#45475A"), specialKeyBgColor=Color.parseColor("#45475A"), keyTextColor=Color.parseColor("#CDD6F4"), specialKeyTextColor=Color.parseColor("#A6ADC8"), accentColor=Color.parseColor("#CBA6F7"), keyCornerRadius=12f, keyStyle=LiyoTheme.KeyStyle.ROUNDED),
            LiyoTheme(id="dracula", name="Dracula", author="Death Legion", isDark=true, keyboardBgColor=Color.parseColor("#282A36"), keyBgColor=Color.parseColor("#44475A"), keyPressedBgColor=Color.parseColor("#6272A4"), specialKeyBgColor=Color.parseColor("#6272A4"), keyTextColor=Color.parseColor("#F8F8F2"), specialKeyTextColor=Color.parseColor("#BD93F9"), accentColor=Color.parseColor("#BD93F9"), keyCornerRadius=8f, keyStyle=LiyoTheme.KeyStyle.MATERIAL),
            LiyoTheme(id="nord", name="Nord", author="Death Legion", isDark=true, keyboardBgColor=Color.parseColor("#2E3440"), keyBgColor=Color.parseColor("#3B4252"), keyPressedBgColor=Color.parseColor("#434C5E"), specialKeyBgColor=Color.parseColor("#434C5E"), keyTextColor=Color.parseColor("#D8DEE9"), specialKeyTextColor=Color.parseColor("#81A1C1"), accentColor=Color.parseColor("#88C0D0"), keyCornerRadius=10f, keyStyle=LiyoTheme.KeyStyle.ROUNDED),
            LiyoTheme(id="tokyo_night", name="Tokyo Night", author="Death Legion", isDark=true, keyboardBgColor=Color.parseColor("#1A1B26"), keyBgColor=Color.parseColor("#24283B"), keyPressedBgColor=Color.parseColor("#414868"), specialKeyBgColor=Color.parseColor("#414868"), keyTextColor=Color.parseColor("#C0CAF5"), specialKeyTextColor=Color.parseColor("#7AA2F7"), accentColor=Color.parseColor("#7AA2F7"), keyCornerRadius=14f, keyStyle=LiyoTheme.KeyStyle.PILL),
            LiyoTheme(id="amoled_black", name="AMOLED Black", author="Death Legion", isDark=true, keyboardBgColor=Color.parseColor("#000000"), keyBgColor=Color.parseColor("#0A0A0A"), keyPressedBgColor=Color.parseColor("#1A1A1A"), specialKeyBgColor=Color.parseColor("#1A1A1A"), keyTextColor=Color.parseColor("#FFFFFF"), specialKeyTextColor=Color.parseColor("#666666"), accentColor=Color.parseColor("#4FC3F7"), keyCornerRadius=8f, keyStyle=LiyoTheme.KeyStyle.MINIMAL),
            LiyoTheme(id="neon_cyberpunk", name="Neon Cyberpunk", author="Death Legion", isDark=true, keyboardBgColor=Color.parseColor("#0D0221"), keyBgColor=Color.parseColor("#150734"), keyPressedBgColor=Color.parseColor("#1F0E4E"), specialKeyBgColor=Color.parseColor("#1F0E4E"), keyTextColor=Color.parseColor("#00FF9F"), specialKeyTextColor=Color.parseColor("#FF00FF"), accentColor=Color.parseColor("#00FFFF"), keyCornerRadius=4f, keyStyle=LiyoTheme.KeyStyle.SHARP),
            LiyoTheme(id="material_light", name="Material Light", author="Death Legion", isDark=false, keyboardBgColor=Color.parseColor("#F5F5F5"), keyBgColor=Color.parseColor("#FFFFFF"), keyPressedBgColor=Color.parseColor("#E0E0E0"), specialKeyBgColor=Color.parseColor("#E0E0E0"), keyTextColor=Color.parseColor("#212121"), specialKeyTextColor=Color.parseColor("#757575"), accentColor=Color.parseColor("#6200EE"), keyCornerRadius=8f, keyStyle=LiyoTheme.KeyStyle.MATERIAL),
            LiyoTheme(id="cherry_blossom", name="Cherry Blossom", author="Death Legion", isDark=false, keyboardBgColor=Color.parseColor("#FFF0F5"), keyBgColor=Color.parseColor("#FFE4EC"), keyPressedBgColor=Color.parseColor("#FFCCD5"), specialKeyBgColor=Color.parseColor("#FFB6C1"), keyTextColor=Color.parseColor("#8B2252"), specialKeyTextColor=Color.parseColor("#CD6090"), accentColor=Color.parseColor("#FF1493"), keyCornerRadius=20f, keyStyle=LiyoTheme.KeyStyle.PILL)
        )
    }

    init { themesDir.mkdirs() }
    private fun loadThemes() {
        themes.clear()
        themes.addAll(builtInThemes())
        prefs.getString("custom_themes", null)?.let {
            val type = object : TypeToken<List<LiyoTheme>>() {}.type
            themes.addAll(gson.fromJson<List<LiyoTheme>>(it, type))
        }
        activeThemeId = prefs.getString("active_theme_id", "catppuccin_mocha") ?: "catppuccin_mocha"
    }
    private fun saveThemes() { prefs.edit().putString("custom_themes", gson.toJson(themes.filter { t -> builtInThemes().none { bt -> bt.id == t.id } })).apply() }
    fun getActiveTheme(): LiyoTheme = themes.find { it.id == activeThemeId } ?: builtInThemes().first()
    fun setActiveTheme(themeId: String) { activeThemeId = themeId; prefs.edit().putString("active_theme_id", themeId).apply() }
    fun getAllThemes(): List<LiyoTheme> = themes.toList()
    fun getThemeById(id: String): LiyoTheme? = themes.find { it.id == id }
    fun saveCustomTheme(theme: LiyoTheme) { themes.removeIf { it.id == theme.id }; themes.add(theme); saveThemes() }
    fun deleteCustomTheme(themeId: String) { themes.removeIf { it.id == themeId && builtInThemes().none { bt -> bt.id == themeId } }; saveThemes() }
    fun exportTheme(themeId: String): File? { val t = themes.find { it.id == themeId } ?: return null; val f = File(themesDir, "${t.id}.json"); f.writeText(t.toJson()); return f }
    fun importTheme(json: String): LiyoTheme? { return try { val t = LiyoTheme.fromJson(json); saveCustomTheme(t); t } catch (e: Exception) { null } }
}
