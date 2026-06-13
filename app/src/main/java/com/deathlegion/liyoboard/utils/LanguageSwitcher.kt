package com.deathlegion.liyoboard.utils

import android.content.Context

/**
 * LanguageSwitcher - Handles switching between Sinhala, English, Tamil
 */
class LanguageSwitcher(private val context: Context) {

    private val languageNames = mapOf(
        "en_US" to "English",
        "si_LK" to "සිංහල (Sinhala)",
        "ta_LK" to "தமிழ் (Tamil)",
        "ta_IN" to "தமிழ் (Tamil - India)"
    )

    private val languageCodes = mapOf(
        "en_US" to "en",
        "si_LK" to "si",
        "ta_LK" to "ta",
        "ta_IN" to "ta"
    )

    fun getLanguageName(code: String): String {
        return languageNames[code] ?: code
    }

    fun getLanguageCode(subtype: String): String {
        return languageCodes[subtype] ?: "en"
    }

    fun getSupportedLanguages(): List<Pair<String, String>> {
        return languageNames.entries.map { it.key to it.value }
    }

    fun getNextLanguage(current: String): String {
        val languages = languageNames.keys.toList()
        val currentIndex = languages.indexOf(current)
        return if (currentIndex >= 0 && currentIndex < languages.size - 1) {
            languages[currentIndex + 1]
        } else {
            languages[0]
        }
    }
}
