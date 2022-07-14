package com.linku.im.screen.overall

import android.util.Log
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.linku.data.usecase.MessageUseCases
import com.linku.domain.Auth
import com.linku.domain.Resource
import com.linku.domain.entity.Message
import com.linku.im.Contract.SAVED_DARK_MODE
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OverallViewModel @Inject constructor(
    private val messageUseCases: MessageUseCases
) : BaseViewModel<OverallState, OverallEvent>(OverallState()) {
    init {
        onEvent(OverallEvent.RestoreDarkMode)
        onEvent(OverallEvent.ObserveCurrentUser { userId ->
            if (userId == null) OverallEvent.Disconnect
            else onEvent(OverallEvent.InitSession(userId))
        })
    }

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages get() = _messages.asStateFlow()

    private lateinit var navController: NavController
    private lateinit var scaffoldState: ScaffoldState
    private lateinit var coroutineScope: CoroutineScope

    override fun onEvent(event: OverallEvent) {
        when (event) {
            is OverallEvent.InitSession -> {
                viewModelScope.launch {
                    val userId = Auth.currentUID
                    checkNotNull(userId)
                    debug { Log.v(TAG, "The message channel is initializing...") }
                    val resource = messageUseCases.initSessionUseCase(
                        uid = userId,
                        scope = viewModelScope
                    )
                    when (resource) {
                        Resource.Loading -> {}
                        is Resource.Success -> {
                            debug {
                                Log.d(TAG, "Message channel initialized successfully.")
                            }
                            onEvent(OverallEvent.Dispatcher)
                        }
                        is Resource.Failure -> {
                            debug {
                                Log.e(
                                    TAG, "Message channel initialization failed" +
                                            "(${resource.message})."
                                )
                            }
                            launch {
                                delay(3000)
                                debug { Log.v(TAG, "Retrying...") }
                                onEvent(OverallEvent.InitSession(userId))
                            }
                        }
                    }
                }
            }
            OverallEvent.Dispatcher -> {
                viewModelScope.launch {
                    when (messageUseCases.dispatcherUseCase()) {
                        Resource.Loading -> debug { Log.v(TAG, "Dispatcher subscribing...") }
                        is Resource.Success -> {
                            debug { Log.d(TAG, "Dispatcher subscribed successfully.") }
                            onEvent(OverallEvent.ObserveMessages)
                        }
                        is Resource.Failure -> debug {
                            Log.e(TAG, "Failed to subscribe Dispatcher.")
                        }
                    }
                }
            }

            OverallEvent.ObserveMessages -> {
                _state.value = state.value.copy(
                    online = true,
                    title = application.getString(R.string.app_name)
                )
                messageUseCases.observeMessagesUseCase(viewModelScope)
                    .onEach {
                        _messages.value = it
                        debug { Log.v(TAG, "Message Received: ${it.lastOrNull()?.content}") }
                    }
                    .launchIn(viewModelScope)
            }

            OverallEvent.RestoreDarkMode -> {
                val isDarkMode = MMKV.defaultMMKV().getBoolean(SAVED_DARK_MODE, false)
                _state.value = state.value.copy(
                    isDarkMode = isDarkMode
                )
            }
            OverallEvent.PopBackStack -> navController.popBackStack()

            OverallEvent.ToggleTheme -> _state.value =
                state.value.copy(isDarkMode = !state.value.isDarkMode)

            OverallEvent.Disconnect -> viewModelScope.launch { messageUseCases.closeSessionUseCase() }
            is OverallEvent.Navigate -> navController.navigate(event.screen.route)

            is OverallEvent.NavigateWithArgs -> navController.navigate(event.route)
            is OverallEvent.InitNavController -> {
                navController = event.navController
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    _state.value = state.value.copy(
                        currentScreen = Screen.valueOf(destination.route ?: "")
                    )
                    deliverNavigationUI(state.value.currentScreen)
                }
            }
            is OverallEvent.ObserveCurrentUser -> {
                Auth.observeCurrent
                    .onEach { event.observer(it) }
                    .launchIn(viewModelScope)
            }
            is OverallEvent.InitScaffoldState -> {
                coroutineScope = event.coroutineScope
                scaffoldState = event.scaffoldState
            }
        }
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

}