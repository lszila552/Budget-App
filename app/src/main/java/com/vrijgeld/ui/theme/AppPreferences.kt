package com.vrijgeld.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

enum class AppTheme { SYSTEM, LIGHT, DARK }

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var theme: AppTheme
        get() = AppTheme.valueOf(prefs.getString(KEY_THEME, AppTheme.DARK.name) ?: AppTheme.DARK.name)
        set(v) = prefs.edit { putString(KEY_THEME, v.name) }

    var accentIndex: Int
        get() = prefs.getInt(KEY_ACCENT, 0)
        set(v) = prefs.edit { putInt(KEY_ACCENT, v) }

    var onboardingDone: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING, false)
        set(v) = prefs.edit { putBoolean(KEY_ONBOARDING, v) }

    var biometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC, true)
        set(v) = prefs.edit { putBoolean(KEY_BIOMETRIC, v) }

    companion object {
        private const val KEY_THEME      = "theme"
        private const val KEY_ACCENT     = "accent_index"
        private const val KEY_ONBOARDING = "onboarding_done"
        private const val KEY_BIOMETRIC  = "biometric_enabled"
    }
}
