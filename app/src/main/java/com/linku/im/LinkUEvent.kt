package com.linku.im

/**
 * The overall EventSet includes restore and initialize events.
 *
 * These can be invoked in [LinkUViewModel]
 * @see LinkUViewModel
 */
sealed class LinkUEvent {
    object InitConfig : LinkUEvent()
    object ToggleDarkMode : LinkUEvent()
    data class OnTheme(val tid: Int, val isDarkMode: Boolean) : LinkUEvent()
    object Disconnect : LinkUEvent()
    object InitSession : LinkUEvent()
    object Premium : LinkUEvent()
}
