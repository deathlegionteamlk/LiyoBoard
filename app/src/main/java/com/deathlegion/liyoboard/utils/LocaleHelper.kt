package com.deathlegion.liyoboard.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

/**
 * LocaleHelper - Handles locale changes for Sinhala/Tamil/English
 */
object LocaleHelper {

    fun setLocale(context: Context, languageCode: String): Context {
        val locale = when (languageCode) {
            "si" -> Locale("si", "LK")
            "ta" -> Locale("ta", "LK")
            else -> Locale("en", "US")
        }

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("liyoboard_prefs", Context.MODE_PRIVATE)
        return prefs.getString("app_language", "en") ?: "en"
    }

    fun saveLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("liyoboard_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("app_language", languageCode).apply()
    }
}
