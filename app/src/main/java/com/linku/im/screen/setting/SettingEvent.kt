package com.linku.im.screen.setting

import android.net.Uri

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
        data class WriteThemeToUri(val uri: Uri, val tid: Int) : Themes()
        data class Import(val uri: Uri) : Themes()
        object ImportFromClipboard : Themes()
    }

    sealed class Language : SettingEvent() {

    }
}
