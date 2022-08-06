package com.linku.im

import com.linku.im.screen.Screen

/**
 * The overall EventSet includes restore and initialize events.
 *
 * These can be invoked in [LinkUViewModel]
 * @see LinkUViewModel
 */
sealed class LinkUEvent {
    object InitConfig : LinkUEvent()
    object ToggleDarkMode : LinkUEvent()
    object ToggleDynamic : LinkUEvent()
    object Disconnect : LinkUEvent()
    object PopBackStack : LinkUEvent()

    object InitSession : LinkUEvent()

    data class Navigate(val screen: Screen) : LinkUEvent()
    data class NavigateWithArgs(val route: String) : LinkUEvent()
}
