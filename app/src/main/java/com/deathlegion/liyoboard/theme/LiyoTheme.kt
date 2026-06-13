package com.deathlegion.liyoboard.theme

import android.graphics.Color
import com.google.gson.annotations.SerializedName

/**
 * LiyoTheme - Complete theme definition for the keyboard
 * Every visual aspect is customizable
 */
data class LiyoTheme(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("author") val author: String = "Death Legion",
    @SerializedName("version") val version: Int = 1,
    @SerializedName("isDark") val isDark: Boolean = false,
    @SerializedName("isPremium") val isPremium: Boolean = false,

    // Background colors
    @SerializedName("keyboardBgColor") val keyboardBgColor: Int = Color.parseColor("#1E1E2E"),
    @SerializedName("keyBgColor") val keyBgColor: Int = Color.parseColor("#2D2D44"),
    @SerializedName("keyPressedBgColor") val keyPressedBgColor: Int = Color.parseColor("#3D3D5C"),
    @SerializedName("specialKeyBgColor") val specialKeyBgColor: Int = Color.parseColor("#3D3D5C"),

    // Text colors
    @SerializedName("keyTextColor") val keyTextColor: Int = Color.parseColor("#E0E0E0"),
    @SerializedName("keyPressedTextColor") val keyPressedTextColor: Int = Color.WHITE,
    @SerializedName("specialKeyTextColor") val specialKeyTextColor: Int = Color.parseColor("#A0A0C0"),
    @SerializedName("suggestionTextColor") val suggestionTextColor: Int = Color.parseColor("#B0B0D0"),

    // Key shape
    @SerializedName("keyCornerRadius") val keyCornerRadius: Float = 12f,
    @SerializedName("keyBorderWidth") val keyBorderWidth: Float = 0f,
    @SerializedName("keyBorderColor") val keyBorderColor: Int = Color.TRANSPARENT,
    @SerializedName("keyShadowRadius") val keyShadowRadius: Float = 0f,
    @SerializedName("keyShadowColor") val keyShadowColor: Int = Color.TRANSPARENT,
    @SerializedName("keyGapHorizontal") val keyGapHorizontal: Float = 4f,
    @SerializedName("keyGapVertical") val keyGapVertical: Float = 4f,
    @SerializedName("keyHeight") val keyHeight: Float = 48f, // dp
    @SerializedName("keyWidth") val keyWidth: Float = 0f, // 0 = auto

    // Toolbar
    @SerializedName("toolbarBgColor") val toolbarBgColor: Int = Color.parseColor("#1A1A2E"),
    @SerializedName("toolbarIconColor") val toolbarIconColor: Int = Color.parseColor("#A0A0C0"),
    @SerializedName("toolbarIconTint") val toolbarIconTint: Int = Color.parseColor("#7C3AED"),

    // Suggestion bar
    @SerializedName("suggestionBarBgColor") val suggestionBarBgColor: Int = Color.parseColor("#1A1A2E"),
    @SerializedName("suggestionBarDividerColor") val suggestionBarDividerColor: Int = Color.parseColor("#333355"),

    // Emoji keyboard
    @SerializedName("emojiBgColor") val emojiBgColor: Int = Color.parseColor("#1E1E2E"),
    @SerializedName("emojiCategoryBgColor") val emojiCategoryBgColor: Int = Color.parseColor("#2D2D44"),
    @SerializedName("emojiCategorySelectedColor") val emojiCategorySelectedColor: Int = Color.parseColor("#7C3AED"),

    // Clipboard panel
    @SerializedName("clipboardBgColor") val clipboardBgColor: Int = Color.parseColor("#1E1E2E"),
    @SerializedName("clipboardItemBgColor") val clipboardItemBgColor: Int = Color.parseColor("#2D2D44"),

    // Fonts
    @SerializedName("keyboardFontName") val keyboardFontName: String? = null,
    @SerializedName("keyTextSize") val keyTextSize: Float = 18f,

    // Animations
    @SerializedName("keyPressAnimation") val keyPressAnimation: KeyPressAnimation = KeyPressAnimation.SCALE,
    @SerializedName("keyPressAnimationDuration") val keyPressAnimationDuration: Long = 100,

    // Gradient support
    @SerializedName("keyboardBgGradientStart") val keyboardBgGradientStart: Int? = null,
    @SerializedName("keyboardBgGradientEnd") val keyboardBgGradientEnd: Int? = null,
    @SerializedName("keyboardBgGradientAngle") val keyboardBgGradientAngle: Int = 0,

    // Image background support
    @SerializedName("keyboardBgImagePath") val keyboardBgImagePath: String? = null,
    @SerializedName("keyboardBgImageAlpha") val keyboardBgImageAlpha: Float = 0.3f,

    // Key style
    @SerializedName("keyStyle") val keyStyle: KeyStyle = KeyStyle.ROUNDED,

    // Accent color for highlights
    @SerializedName("accentColor") val accentColor: Int = Color.parseColor("#7C3AED"),

    // Preview
    @SerializedName("previewImagePath") val previewImagePath: String? = null
) {
    enum class KeyPressAnimation {
        NONE, SCALE, RIPPLE, GLOW
    }

    enum class KeyStyle {
        ROUNDED, SHARP, CIRCLE, PILL, MINIMAL, MATERIAL, IOS, NEUMORPHIC
    }

    fun toJson(): String = com.google.gson.Gson().toJson(this)

    companion object {
        fun fromJson(json: String): LiyoTheme = com.google.gson.Gson().fromJson(json, LiyoTheme::class.java)
    }
}
