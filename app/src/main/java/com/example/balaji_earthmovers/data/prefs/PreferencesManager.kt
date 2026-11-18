package com.example.balaji_earthmovers.data.prefs

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    MARATHI("mr", "मराठी (Marathi)"),
    HINDI("hi", "हिंदी (Hindi)")
}

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _appPin = MutableStateFlow(prefs.getString(KEY_APP_PIN, DEFAULT_APP_PIN) ?: DEFAULT_APP_PIN)
    val appPin: StateFlow<String> = _appPin.asStateFlow()

    private val _adminPin = MutableStateFlow(prefs.getString(KEY_ADMIN_PIN, DEFAULT_ADMIN_PIN) ?: DEFAULT_ADMIN_PIN)
    val adminPin: StateFlow<String> = _adminPin.asStateFlow()

    private val _language = MutableStateFlow(loadLanguage())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private fun loadThemeMode(): AppThemeMode {
        val saved = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        return try {
            AppThemeMode.valueOf(saved ?: AppThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    private fun loadLanguage(): AppLanguage {
        val saved = prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.name)
        return try {
            AppLanguage.valueOf(saved ?: AppLanguage.ENGLISH.name)
        } catch (e: Exception) {
            AppLanguage.ENGLISH
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setAppPin(newPin: String) {
        prefs.edit().putString(KEY_APP_PIN, newPin).apply()
        _appPin.value = newPin
    }

    fun setAdminPin(newPin: String) {
        prefs.edit().putString(KEY_ADMIN_PIN, newPin).apply()
        _adminPin.value = newPin
    }

    fun setLanguage(lang: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, lang.name).apply()
        _language.value = lang
    }

    fun verifyAppPin(pin: String): Boolean {
        return pin.trim() == _appPin.value.trim()
    }

    fun verifyAdminPin(pin: String): Boolean {
        return pin.trim() == _adminPin.value.trim()
    }

    companion object {
        private const val PREFS_NAME = "balaji_app_prefs"
        private const val KEY_APP_PIN = "key_app_pin"
        private const val KEY_ADMIN_PIN = "key_admin_pin"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_LANGUAGE = "key_language"

        const val DEFAULT_APP_PIN = "2838"
        const val DEFAULT_ADMIN_PIN = "2476"

        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
