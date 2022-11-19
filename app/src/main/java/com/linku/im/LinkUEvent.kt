package com.linku.im

/**
 * The overall EventSet includes restore and initialize events.
 *
 * These can be invoked in [LinkUViewModel]
 * @see LinkUViewModel
 */
sealed interface LinkUEvent {
    object InitConfig : LinkUEvent
    object ToggleDarkMode : LinkUEvent
    data class OnTheme(val tid: Int, val isDarkMode: Boolean) : LinkUEvent
    data class OnNativeSnackBar(val target: Boolean) : LinkUEvent
    object Disconnect : LinkUEvent
    object InitSession : LinkUEvent
    object Premium : LinkUEvent
}
