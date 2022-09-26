package com.linku.data.usecase

import android.content.Context
import android.content.SharedPreferences
import com.linku.domain.extension.BooleanPreference
import com.linku.domain.extension.IntNullablePreference
import com.linku.domain.extension.IntPreference
import com.linku.domain.extension.StringPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class SharedPreferenceUseCase @Inject constructor(@ApplicationContext context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
    var isDarkMode: Boolean by BooleanPreference(sharedPreferences, DARK_MODE, false)
    var isDynamicMode: Boolean by BooleanPreference(sharedPreferences, DYNAMIC_MODE, false)
    var currentUID: Int? by IntNullablePreference(sharedPreferences, CURRENT_UID)
    var token: String? by StringPreference(sharedPreferences, TOKEN)
    var isLogMode: Boolean by BooleanPreference(sharedPreferences, LOG_MODE, false)

    var lightTheme: Int by IntPreference(sharedPreferences, LIGHT_THEME, -1)
    var darkTheme: Int by IntPreference(sharedPreferences, DARK_THEME, -1)

    inline fun debug(block: () -> Unit) {
        if (isLogMode) block()
    }

    companion object {
        private const val DARK_MODE = "dark_mode"
        private const val DYNAMIC_MODE = "dynamic_mode"
        private const val CURRENT_UID = "current_uid"
        private const val TOKEN = "token"
        private const val SHARED_SETTINGS = "shared_settings"
        private const val LOG_MODE = "log_mode"
        private const val LIGHT_THEME = "light_theme"
        private const val DARK_THEME = "dark_theme"
    }
}