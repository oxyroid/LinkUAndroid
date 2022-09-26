package com.linku.im.screen.setting

import com.linku.domain.entity.local.Theme

sealed class SettingState {
    class Notification() : SettingState()
    class PrivacySecurity() : SettingState()
    class DataStorage() : SettingState()
    data class Themes(
        val loading: Boolean = false,
        val themes: List<Theme> = emptyList(),
        val currentTheme: Int = -1,
        val defaultLightTheme: Int = -1,
        val defaultDarkTheme: Int = -1
    ) : SettingState()

    class Language() : SettingState()
}
