package com.linku.im.ui.components.notify

sealed class NotifyMode {
    sealed class Duration(val duration: kotlin.Long) : NotifyMode() {
        object Short : Duration(400L)
        object Medium : Duration(800L)
        object Long : Duration(1200L)
    }

    object Pinned : NotifyMode()
}
