package com.linku.im.screen.setting

import android.net.Uri

sealed interface SettingEvent {
    sealed interface Notification : SettingEvent {

    }

    sealed interface PrivacySecurity : SettingEvent {

    }

    sealed interface DataStorage : SettingEvent {

    }

    sealed interface Themes : SettingEvent {
        object Init : Themes
        object ToggleIsDarkMode : Themes
        data class SelectThemes(val tid: Int) : Themes
        data class WriteThemeToUri(val uri: Uri, val tid: Int) : Themes
        data class Import(val uri: Uri) : Themes
        object ImportFromClipboard : Themes
        object PressedCancel : Themes
        data class Press(val tid: Int) : Themes
        object DeletePressedTheme : Themes
    }

    sealed class Language : SettingEvent {

    }
}
