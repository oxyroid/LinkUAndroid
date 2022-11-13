package com.linku.im.ui.components.notify

sealed class NotifyMode {
    sealed class Duration(val duration: kotlin.Long) : NotifyMode() {
        object Short : Duration(NotifyDurationDefaults.Short)
        object Medium : Duration(NotifyDurationDefaults.Medium)
        object Long : Duration(NotifyDurationDefaults.Long)
    }

    object Pinned : NotifyMode()
}

private object NotifyDurationDefaults {
    const val Short = 400L
    const val Medium = 800L
    const val Long = 1200L
}
