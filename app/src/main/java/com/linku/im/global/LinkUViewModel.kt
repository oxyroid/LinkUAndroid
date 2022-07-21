package com.linku.im.global

import android.util.Log
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.linku.data.usecase.MessageUseCases
import com.linku.domain.Auth
import com.linku.domain.Resource
import com.linku.im.Constants.SAVED_DARK_MODE
import com.linku.im.R
import com.linku.im.application
import com.linku.im.extension.TAG
import com.linku.im.extension.debug
import com.linku.im.extension.toggle
import com.linku.im.screen.BaseViewModel
import com.linku.im.screen.Screen
import com.tencent.mmkv.MMKV
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LinkUViewModel @Inject constructor(
    private val messageUseCases: MessageUseCases
) : BaseViewModel<LinkUState, LinkUEvent>(LinkUState()) {
    init {
        onEvent(LinkUEvent.RestoreDarkMode)
        onEvent(LinkUEvent.ObserveCurrentUser { userId ->
            if (userId == null) LinkUEvent.Disconnect
            else onEvent(LinkUEvent.InitSession(userId))
        })
    }

    private lateinit var navController: NavController
    private lateinit var scaffoldState: ScaffoldState
    private lateinit var coroutineScope: CoroutineScope

    override fun onEvent(event: LinkUEvent) {
        when (event) {
            is LinkUEvent.InitSession -> initSession()
            LinkUEvent.RestoreDarkMode -> {
                val isDarkMode = MMKV.defaultMMKV().getBoolean(SAVED_DARK_MODE, false)
                _state.value = state.value.copy(
                    isDarkMode = isDarkMode
                )
            }
            LinkUEvent.PopBackStack -> navController.navigateUp()

            LinkUEvent.ToggleDarkMode -> {
                MMKV.defaultMMKV().encode(SAVED_DARK_MODE, !state.value.isDarkMode)
                _state.value =
                    state.value.copy(
                        isDarkMode = !state.value.isDarkMode
                    )
            }

            LinkUEvent.Disconnect -> {
                updateTitle(Title.NoAuth)
                viewModelScope.launch { messageUseCases.closeSession() }
            }
            is LinkUEvent.Navigate -> navController.navigate(event.screen.route)

            is LinkUEvent.NavigateWithArgs -> navController.navigate(event.route)
            is LinkUEvent.InitNavController -> {
                navController = event.navController
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    _state.value = state.value.copy(
                        currentScreen = Screen.valueOf(destination.route ?: "")
                    )
                    deliverNavigationUI(state.value.currentScreen)
                }
            }
            is LinkUEvent.ObserveCurrentUser -> {
                Auth.observeCurrent
                    .onEach { event.observer(it) }
                    .launchIn(viewModelScope)
            }
            is LinkUEvent.InitScaffoldState -> {
                coroutineScope = event.coroutineScope
                scaffoldState = event.scaffoldState
            }
        }
    }

    fun onActions(actions: @Composable RowScope.() -> Unit) {
        _state.value = state.value.copy(
            actions = actions
        )
    }

    private fun deliverNavigationUI(screen: Screen) {
        val oldState = state.value
        _state.value = when (screen) {
            Screen.MainScreen -> oldState.copy(
                icon = Icons.Default.Menu,
                navClick = { scaffoldState.drawerState.toggle(coroutineScope) },
                currentScreen = screen
            )
            Screen.ChatScreen -> oldState.copy(
                icon = Icons.Default.ArrowBack,
                navClick = navController::popBackStack,
                currentScreen = screen
            )
            Screen.LoginScreen -> oldState.copy(
                icon = Icons.Default.ArrowBack,
                navClick = navController::popBackStack,
                currentScreen = screen
            )
            Screen.ProfileScreen -> oldState.copy(
                icon = Icons.Default.ArrowBack,
                navClick = navController::popBackStack,
                currentScreen = screen
            )
            Screen.InfoScreen -> oldState.copy(
                icon = Icons.Default.ArrowBack,
                navClick = navController::popBackStack,
                currentScreen = screen
            )
        }
    }

    private sealed class Title(val text: String) {
        object Default : Title(application.getString(R.string.app_name))
        object Connecting : Title(application.getString(R.string.connecting))
        object ConnectedFailed : Title(application.getString(R.string.connected_failed))
        object NoAuth : Title(application.getString(R.string.no_auth))
    }

    private fun updateTitle(title: Title) {
        _state.value = state.value.copy(
            title = title.text
        )
    }

    private fun initSession() {
        viewModelScope.launch {
            val userId = Auth.currentUID
            checkNotNull(userId)
            updateTitle(Title.Connecting)

            val resource = messageUseCases.initSession(
                uid = userId,
                scope = viewModelScope
            )
            when (resource) {
                Resource.Loading -> {}
                is Resource.Success -> {
                    updateTitle(Title.Default)
                    messageUseCases.observeMessages()
                        .launchIn(this)
                }
                is Resource.Failure -> {
                    updateTitle(Title.ConnectedFailed)
                    launch {
                        delay(3000)
                        debug { Log.v(TAG, "Retrying...") }
                        onEvent(LinkUEvent.InitSession(userId))
                    }
                }
            }
        }
    }

}