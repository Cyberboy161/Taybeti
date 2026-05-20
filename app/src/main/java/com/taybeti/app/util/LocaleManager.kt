package com.taybeti.app.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleManager {

    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_LANGUAGE = "app_language"
    private const val KEY_FIRST_LAUNCH = "first_launch_done"

    val languages = listOf(
        Language("en", "English", "\uD83C\uDF10"),
        Language("de", "Deutsch", "\uD83C\uDDE9\uD83C\uDDEA"),
        Language("ckb", "Kurdi Sorani", null),
        Language("kmr", "Kurdi Kurmanji", null),
        Language("tr", "Türkçe", "\uD83C\uDDF9\uD83C\uDDF7"),
    )

    data class Language(
        val code: String,
        val label: String,
        val flagEmoji: String? // null = use KurdistanFlag composable
    )

    fun isFirstLaunch(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun markLaunched(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_FIRST_LAUNCH, false).commit()
    }

    fun getCurrentLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun setLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANGUAGE, languageCode).commit()
        applyLocale(context, languageCode)
    }

    fun applyLocale(context: Context, languageCode: String) {
        val locale = when (languageCode) {
            "ckb" -> Locale("ckb", "IQ")
            "kmr" -> Locale("kmr", "TR")
            else -> Locale(languageCode)
        }
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
