package com.linku.im.screen.setting

sealed class SettingState {
    class Notification() : SettingState()
    class PrivacySecurity() : SettingState()
    class DataStorage() : SettingState()
    data class Themes(
        val loading: Boolean = false,
        val currentTheme: Int = -1,
        val defaultLightTheme: Int = -1,
        val defaultDarkTheme: Int = -1,
    ) : SettingState()

    class Language() : SettingState()
}
