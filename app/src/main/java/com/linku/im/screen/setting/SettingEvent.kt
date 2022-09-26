package com.linku.im.screen.setting

sealed class SettingEvent {
    sealed class Notification : SettingEvent() {

    }

    sealed class PrivacySecurity : SettingEvent() {

    }

    sealed class DataStorage : SettingEvent() {

    }

    sealed class Themes : SettingEvent() {
        object Init : Themes()
        object ToggleIsDarkMode : Themes()
        data class SelectThemes(val tid: Int) : Themes()
        data class Export(val tid: Int) : Themes()
        object Import : Themes()
        object ImportFromClipboard : Themes()
    }

    sealed class Language : SettingEvent() {

    }
}
