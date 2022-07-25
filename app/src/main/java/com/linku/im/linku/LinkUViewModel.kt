package com.linku.im.linku

import android.util.Log
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination
import com.linku.data.usecase.MessageUseCases
import com.linku.domain.Auth
import com.linku.domain.Resource
import com.linku.domain.eventOf
import com.linku.im.Constants.SAVED_DARK_MODE
import com.linku.im.Constants.SAVED_DYNAMIC_MODE
import com.linku.im.R
import com.linku.im.applicationContext
import com.linku.im.extension.TAG
import com.linku.im.extension.debug
import com.linku.im.extension.toggle
import com.linku.im.screen.BaseViewModel
import com.linku.im.screen.Screen
import com.linku.im.ui.theme.supportDynamic
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
        onEvent(LinkUEvent.InitConfig)
        onEvent(LinkUEvent.ObserveCurrentUser { userId ->
            if (userId == null) LinkUEvent.Disconnect
            else onEvent(LinkUEvent.InitSession(userId))
        })
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private lateinit var drawerState: DrawerState
    private lateinit var coroutineScope: CoroutineScope

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onEvent(event: LinkUEvent) {
        when (event) {
            is LinkUEvent.InitSession -> initSession()
            LinkUEvent.InitConfig -> initConfig()
            LinkUEvent.PopBackStack -> _state.value = readable.copy(
                navigateUp = eventOf(Unit)
            )

            LinkUEvent.ToggleDarkMode -> {
                MMKV.defaultMMKV().encode(SAVED_DARK_MODE, !readable.isDarkMode)
                _state.value = readable.copy(
                    isDarkMode = !readable.isDarkMode
                )
            }

            LinkUEvent.Disconnect -> {
                updateTitle(Label.NoAuth)
                viewModelScope.launch { messageUseCases.closeSession() }
            }
            is LinkUEvent.Navigate -> {
                _state.value = readable.copy(
                    navigate = eventOf(event.screen.route)
                )
            }

            is LinkUEvent.NavigateWithArgs -> {
                _state.value = readable.copy(
                    navigate = eventOf(event.route)
                )
            }
            is LinkUEvent.InitNavController -> {

            }
            is LinkUEvent.ObserveCurrentUser -> {
                Auth.observeCurrent
                    .onEach { event.observer(it) }
                    .launchIn(viewModelScope)
            }
            is LinkUEvent.InitScaffoldState -> {
                coroutineScope = event.coroutineScope
                drawerState = event.drawerState
            }
            LinkUEvent.ToggleDynamic -> {
                MMKV.defaultMMKV().encode(SAVED_DYNAMIC_MODE, !readable.dynamicEnabled)
                _state.value = readable.copy(
                    dynamicEnabled = !state.value.dynamicEnabled
                )
            }
        }
    }

    fun onActions(actions: @Composable RowScope.() -> Unit) {
        _state.value = readable.copy(
            actions = actions
        )
    }

    fun onTitle(title: @Composable () -> Unit) {
        _state.value = readable.copy(
            title = title
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun deliverNavigationUI(screen: Screen) {
        _state.value = when (screen) {
            Screen.MainScreen -> readable.copy(
                icon = Icons.Default.Menu,
                navClick = { drawerState.toggle(coroutineScope) },
                currentScreen = screen
            )
            Screen.ChatScreen -> readable.copy(
                icon = Icons.Default.ArrowBack,
                navClick = ::navigateUp,
                currentScreen = screen
            )
            Screen.LoginScreen -> readable.copy(
                icon = Icons.Default.ArrowBack,
                navClick = ::navigateUp,
                currentScreen = screen
            )
            Screen.ProfileScreen -> readable.copy(
                icon = Icons.Default.ArrowBack,
                navClick = ::navigateUp,
                currentScreen = screen
            )
            Screen.QueryScreen -> readable.copy(
                icon = Icons.Default.ArrowBack,
                navClick = ::navigateUp,
                currentScreen = screen
            )
        }
    }

    private sealed class Label(val text: String) {
        object Default : Label(applicationContext.getString(R.string.app_name))
        object Connecting : Label(applicationContext.getString(R.string.connecting))
        object ConnectedFailed : Label(applicationContext.getString(R.string.connected_failed))
        object NoAuth : Label(applicationContext.getString(R.string.no_auth))
    }

    private fun updateTitle(label: Label) {
        _state.value = readable.copy(
            label = label.text
        )
    }

    private fun initSession() {
        viewModelScope.launch {
            val userId = Auth.currentUID
            checkNotNull(userId)
            updateTitle(Label.Connecting)

            val resource = messageUseCases.initSession(
                uid = userId,
                scope = viewModelScope
            )
            when (resource) {
                Resource.Loading -> {}
                is Resource.Success -> {
                    updateTitle(Label.Default)
                    messageUseCases.observeMessages()
                        .launchIn(this)
                }
                is Resource.Failure -> {
                    updateTitle(Label.ConnectedFailed)
                    launch {
                        delay(3000)
                        debug { Log.v(TAG, "Retrying...") }
                        onEvent(LinkUEvent.InitSession(userId))
                    }
                }
            }
        }
    }

    private fun initConfig() {
        val isDarkMode = MMKV.defaultMMKV().getBoolean(SAVED_DARK_MODE, false)
        val enableDynamic =
            MMKV.defaultMMKV().getBoolean(SAVED_DYNAMIC_MODE, supportDynamic)
        _state.value = readable.copy(
            isDarkMode = isDarkMode,
            dynamicEnabled = enableDynamic
        )
    }

    private fun navigateUp() {
        _state.value = readable.copy(
            navigateUp = eventOf(Unit)
        )
    }

    fun currentScreen(destination: NavDestination) {
        _state.value = readable.copy(
            currentScreen = Screen.valueOf(destination.route ?: "")
        )
        deliverNavigationUI(readable.currentScreen)
    }

}