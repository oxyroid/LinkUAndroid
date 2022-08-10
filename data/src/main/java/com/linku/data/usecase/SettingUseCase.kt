package com.linku.data.usecase

import android.content.Context
import android.content.SharedPreferences
import com.linku.domain.extension.BooleanPreference
import com.linku.domain.extension.IntPreference
import com.linku.domain.extension.StringPreference

class SettingUseCase(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
    var isDarkMode: Boolean by BooleanPreference(sharedPreferences, DARK_MODE, false)
    var isDynamicMode: Boolean by BooleanPreference(sharedPreferences, DYNAMIC_MODE, false)
    var currentUID: Int? by IntPreference(sharedPreferences, CURRENT_UID)
    var token: String? by StringPreference(sharedPreferences, TOKEN)

    companion object {
        const val DARK_MODE = "dark_mode"
        const val DYNAMIC_MODE = "dynamic_mode"
        const val CURRENT_UID = "current_uid"
        const val TOKEN = "token"
        const val SHARED_SETTINGS = "shared_settings"
    }
}