package com.linku.im.screen.overall

import android.util.Log
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.linku.data.usecase.AuthUseCases
import com.linku.domain.Auth
import com.linku.domain.Resource
import com.linku.domain.entity.Message
import com.linku.domain.service.ChatSocketService
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OverallViewModel @Inject constructor(
    private val socketService: ChatSocketService,
    private val authUseCases: AuthUseCases
) : BaseViewModel<OverallState, OverallEvent>(OverallState()) {
    init {
        onEvent(OverallEvent.RestoreDarkMode)
        onEvent(OverallEvent.ObserveCurrentUser { user ->
            onEvent(
                if (user == null) OverallEvent.Disconnect
                else OverallEvent.RestoreCookie
            )
        })
    }

    private val _messages = MutableSharedFlow<Message>()
    val messages = _messages.asSharedFlow()

    private lateinit var navController: NavController
    private lateinit var scaffoldState: ScaffoldState
    private lateinit var coroutineScope: CoroutineScope

    override fun onEvent(event: OverallEvent) {
        when (event) {
            OverallEvent.RestoreCookie -> {
                val current = Auth.current ?: return
                val currentPassword = Auth.currentPassword ?: return
                authUseCases.loginUseCase(
                    email = current.email,
                    password = currentPassword
                ).onEach { resource ->
                    when (resource) {
                        Resource.Loading -> debug {
                            Log.e(TAG, "The message channel is initializing...")
                        }
                        is Resource.Success -> onEvent(OverallEvent.InitSession(resource.data.id))
                        is Resource.Failure -> debug {
                            Log.e(TAG, "Login with cookie failed(${resource.message})")
                        }
                    }
                }.launchIn(viewModelScope)
            }
            OverallEvent.RestoreDarkMode -> {
                val isDarkMode = MMKV.defaultMMKV().getBoolean(SAVED_DARK_MODE, false)
                _state.value = state.value.copy(
                    isDarkMode = isDarkMode
                )
            }
            OverallEvent.PopBackStack -> {
                navController.popBackStack()
            }
            OverallEvent.ToggleTheme -> {
                _state.value = state.value.copy(
                    isDarkMode = !state.value.isDarkMode
                )
            }
            OverallEvent.Disconnect -> viewModelScope.launch {
                socketService.closeSession()
            }
            is OverallEvent.Navigate -> {
                navController.navigate(event.screen.route)
            }
            is OverallEvent.InitSession -> {
                viewModelScope.launch {
                    val currentUser = Auth.current
                    checkNotNull(currentUser)
                    when (val unitResource = socketService.initSession()) {
                        Resource.Loading -> {}
                        is Resource.Success -> {
                            socketService.observeMessages()
                                .onEach { message ->
                                    _messages.emit(message)
                                }
                                .launchIn(viewModelScope)

                            debug {
                                Log.e(TAG, "Message channel initialized successfully.")
                            }
                        }
                        is Resource.Failure -> {
                            debug {
                                Log.e(
                                    TAG, "Message channel initialization failed" +
                                            "(${unitResource.message})."
                                )
                            }
                        }
                    }
                }
            }
            is OverallEvent.NavigateSpecial -> {
                navController.navigate(event.route)
            }
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
                    .onEach {
                        event.observer(it)
                    }
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
                title = application.getString(R.string.app_name),
                navClick = {
                    scaffoldState.drawerState.toggle(coroutineScope)
                },
                actions = {
                    IconButton(
                        onClick = {
                            onEvent(OverallEvent.ToggleTheme)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                currentScreen = screen
            )
            Screen.ChatScreen -> oldState.copy(
                icon = Icons.Default.ArrowBack,
                title = application.getString(R.string.app_name),
                navClick = navController::popBackStack,
                actions = { },
                currentScreen = screen
            )
            Screen.LoginScreen -> oldState.copy(
                icon = Icons.Default.ArrowBack,
                title = application.getString(R.string.screen_login_title),
                navClick = navController::popBackStack,
                actions = { },
                currentScreen = screen
            )
            Screen.ProfileScreen -> oldState.copy(
                icon = Icons.Default.ArrowBack,
                title = application.getString(R.string.screen_profile_title),
                navClick = navController::popBackStack,
                actions = { },
                currentScreen = screen
            )
            Screen.InfoScreen -> oldState.copy(
                icon = Icons.Default.ArrowBack,
                title = application.getString(R.string.screen_info_title),
                navClick = navController::popBackStack,
                actions = { },
                currentScreen = screen
            )
        }
    }
}