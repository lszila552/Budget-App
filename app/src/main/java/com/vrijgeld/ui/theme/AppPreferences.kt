package com.vrijgeld.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme { SYSTEM, LIGHT, DARK }

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var theme: AppTheme
        get() = AppTheme.valueOf(prefs.getString(KEY_THEME, AppTheme.DARK.name) ?: AppTheme.DARK.name)
        set(v) {
            prefs.edit { putString(KEY_THEME, v.name) }
            _themeFlow.value = v
        }

    var accentIndex: Int
        get() = prefs.getInt(KEY_ACCENT, 0)
        set(v) {
            prefs.edit { putInt(KEY_ACCENT, v) }
            _accentFlow.value = v
        }

    var onboardingDone: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING, false)
        set(v) = prefs.edit { putBoolean(KEY_ONBOARDING, v) }

    var biometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC, true)
        set(v) = prefs.edit { putBoolean(KEY_BIOMETRIC, v) }

    init {
        _themeFlow.value  = theme
        _accentFlow.value = accentIndex
    }

    companion object {
        private const val KEY_THEME      = "theme"
        private const val KEY_ACCENT     = "accent_index"
        private const val KEY_ONBOARDING = "onboarding_done"
        private const val KEY_BIOMETRIC  = "biometric_enabled"

        private val _themeFlow  = MutableStateFlow(AppTheme.DARK)
        private val _accentFlow = MutableStateFlow(0)

        val themeFlow  = _themeFlow.asStateFlow()
        val accentFlow = _accentFlow.asStateFlow()
    }
}
