package com.linku.im.screen.overall

import androidx.compose.material.ScaffoldState
import androidx.navigation.NavController
import com.linku.domain.entity.User
import com.linku.im.screen.Screen
import kotlinx.coroutines.CoroutineScope

/**
 * The overall EventSet includes restore and initialize events.
 *
 * These can be invoked in [OverallViewModel]
 * @see OverallViewModel
 */
sealed class OverallEvent {
    /**
     * Login with saved token to restore the session cookie.
     */
    object RestoreCookie : OverallEvent()

    /**
     * Restore the application is in dark mode or not last time.
     */
    object RestoreDarkMode : OverallEvent()

    /**
     * Change theme state to effect composable.
     */
    object ToggleTheme : OverallEvent()

    /**
     * Disconnect the websocket flow.
     *
     * As usually, this event should be invoked after user logging out.
     */
    object Disconnect : OverallEvent()

    /**
     * Pop Back Screen from the stack.
     *
     * This event is a wrapper of [NavController.popBackStack]
     */
    object PopBackStack : OverallEvent()

    /**
     * Initialize navController.
     * @suppress This event must be called before other Navigation Event.
     * @param navController The remembered navController in your root composable.
     */
    data class InitNavController(val navController: NavController) : OverallEvent()

    /**
     * Initialize scaffoldState.
     * @suppress This event must be called before other Navigation Event.
     * @param coroutineScope Toggle state need scope.
     * @param scaffoldState The remembered scaffoldState in your root composable.
     */
    data class InitScaffoldState(
        val coroutineScope: CoroutineScope,
        val scaffoldState: ScaffoldState
    ) : OverallEvent()

    /**
     * Initialize the websocket session.
     * @suppress This event should be called after user logging in.
     * @param uid The user ID.
     */
    data class InitSession(val uid: Int) : OverallEvent()

    /**
     * Observe the current user changed event.
     *
     * This event should be called only once.
     *
     * In fact, you can collect the observer and add custom event: RemoveObserve to achieve multi-observer
     */
    data class ObserveCurrentUser(val observer: (User?) -> Unit) : OverallEvent()
    data class Navigate(val screen: Screen) : OverallEvent()
    data class NavigateSpecial(val route: String) : OverallEvent()
}
