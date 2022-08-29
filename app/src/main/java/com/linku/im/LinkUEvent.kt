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
    object Disconnect : LinkUEvent()
    object InitSession : LinkUEvent()
}
