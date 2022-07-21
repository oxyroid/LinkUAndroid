package com.linku.im.global

import androidx.compose.material.ScaffoldState
import androidx.navigation.NavController
import com.linku.im.screen.Screen
import kotlinx.coroutines.CoroutineScope

/**
 * The overall EventSet includes restore and initialize events.
 *
 * These can be invoked in [LinkUViewModel]
 * @see LinkUViewModel
 */
sealed class LinkUEvent {
    /**
     * Restore the application is in dark mode or not last time.
     */
    object RestoreDarkMode : LinkUEvent()

    /**
     * Change theme state to effect composable.
     */
    object ToggleDarkMode : LinkUEvent()

    /**
     * Disconnect the websocket flow.
     *
     * As usually, this event should be invoked after user logging out.
     */
    object Disconnect : LinkUEvent()

    /**
     * Pop Back Screen from the stack.
     *
     * This event is a wrapper of [NavController.popBackStack]
     */
    object PopBackStack : LinkUEvent()

    /**
     * Initialize navController.
     * @suppress This event must be called before other Navigation Event.
     * @param navController The remembered navController in your root composable.
     */
    data class InitNavController(val navController: NavController) : LinkUEvent()

    /**
     * Initialize scaffoldState.
     * @suppress This event must be called before other Navigation Event.
     * @param coroutineScope Toggle state need scope.
     * @param scaffoldState The remembered scaffoldState in your root composable.
     */
    data class InitScaffoldState(
        val coroutineScope: CoroutineScope,
        val scaffoldState: ScaffoldState
    ) : LinkUEvent()

    /**
     * Initialize the websocket session.
     * @suppress This event should be called after user logging in.
     * @param uid The user ID.
     */
    data class InitSession(val uid: Int) : LinkUEvent()

    /**
     * Observe the current user changed event.
     *
     * This event should be called only once.
     *
     * In fact, you can collect the observer and add custom event: RemoveObserve to achieve multi-observer
     */
    data class ObserveCurrentUser(val observer: (Int?) -> Unit) : LinkUEvent()
    data class Navigate(val screen: Screen) : LinkUEvent()
    data class NavigateWithArgs(val route: String) : LinkUEvent()
}
