package com.linku.im.screen.global

import android.util.Log
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.linku.domain.Auth
import com.linku.domain.Resource
import com.linku.domain.entity.Message
import com.linku.domain.service.ChatSocketService
import com.linku.domain.usecase.AuthUseCases
import com.linku.im.screen.BaseViewModel
import com.tencent.mmkv.MMKV
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlobalViewModel @Inject constructor(
    private val socketService: ChatSocketService,
    private val authUseCases: AuthUseCases
) : BaseViewModel<GlobalState, GlobalEvent>(GlobalState()) {

    var icon = mutableStateOf(Icons.Default.Menu)
    var title = mutableStateOf("")
    var navClick = mutableStateOf({})
    var actions = mutableStateOf<@Composable RowScope.() -> Unit>(@Composable { })
    var isDarkMode = mutableStateOf(
        MMKV.defaultMMKV().getBoolean(SAVED_DARK_MODE, false)
    )

    companion object {
        const val SAVED_DARK_MODE = "saved:dark-mode"
    }

    private val _messages = MutableSharedFlow<Message>()
    val messages = _messages.asSharedFlow()

    fun connectToChat() {

        disconnect()
        viewModelScope.launch {
            val currentUser = Auth.current
            checkNotNull(currentUser)
            when (
                val resource = socketService.initSession(1)
            ) {
                Resource.Loading -> {}
                is Resource.Success -> {
                    socketService.observeMessages()
                        .onEach { message ->
                            _messages.emit(message)
                        }
                        .launchIn(viewModelScope)
                }
                is Resource.Failure -> {
                    Log.e("GVM", "connectToChat: ${resource.message}")
                }
            }
        }
        socketService.observeClose().onEach { close ->
            Log.e("GVM", "connectToChat: $close")
        }.launchIn(viewModelScope)
    }


    fun disconnect() {
        viewModelScope.launch {
            socketService.closeSession()
        }
    }

    override fun onEvent(event: GlobalEvent) {
        when (event) {
            GlobalEvent.LoginFromCache -> {
                val current = Auth.current ?: return
//                authUseCases.loginUseCase(
//                    email = current.email,
//                    password =
//                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}