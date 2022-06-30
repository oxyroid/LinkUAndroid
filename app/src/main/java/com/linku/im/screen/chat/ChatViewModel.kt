package com.linku.im.screen.chat

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linku.domain.entity.TextMessage
import com.linku.domain.service.ChatSocketService
import com.linku.wrapper.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val socketService: ChatSocketService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _messageTextState = mutableStateOf("")
    val messageTextState: State<String> = _messageTextState

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    private val _chatState = mutableStateOf(ChatState())
    val chatState: State<ChatState> = _chatState

    init {

    }

    fun connectToChat() {
        savedStateHandle.get<String>("cid")?.let { cid ->
            savedStateHandle.get<String>("uid")?.let { uid ->
                viewModelScope.launch {
                    when (val resource = socketService.initSession(uid.toInt(), cid.toInt())) {
                        Resource.Loading -> {}
                        is Resource.Success -> {
                            socketService.observeMessages()
                                .onEach { message ->
                                    val newList = _chatState.value.messages.toMutableList()
                                    newList.add(0, message)
                                    _chatState.value = chatState.value.copy(
                                        messages = newList
                                    )
                                }
                                .launchIn(viewModelScope)
                        }
                        is Resource.Failure -> {
                            _toastEvent.emit(resource.message)
                        }
                    }
                }
                viewModelScope.launch {
                    socketService.observeClose().onEach { close ->
                        val newList = _chatState.value.messages.toMutableList()
                        newList.add(0, TextMessage(0, 0, 0, "Closed! [${close.readReason()}]"))
                        _chatState.value = chatState.value.copy(
                            messages = newList
                        )
                    }
                }
            }
        }
    }

    fun onTextChange(text: String) {
        _messageTextState.value = text
    }

    fun onMessage() {
        if (_messageTextState.value.isBlank()) return
        viewModelScope.launch {
            val message = TextMessage(cid = 1, uid = 0, text = _messageTextState.value)
            socketService.sendMessage(message)
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            socketService.closeSession()
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

}