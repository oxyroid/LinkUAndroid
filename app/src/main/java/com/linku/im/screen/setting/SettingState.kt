package com.linku.im.screen.setting

sealed interface SettingState {
    data class Notification(
        val loading: Boolean = false,
        val isNativeSnackBar: Boolean = true
    ) : SettingState

    class PrivacySecurity() : SettingState
    class DataStorage() : SettingState
    data class Themes(
        val loading: Boolean = false,
        val currentTheme: Int = -1,
        val defaultLightTheme: Int = -1,
        val defaultDarkTheme: Int = -1,
        val currentPressedTheme: Int = -1
    ) : SettingState

    class Language() : SettingState
}
