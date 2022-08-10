package com.linku.im.screen.chat

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.ConversationUseCases
import com.linku.data.usecase.EmojiUseCases
import com.linku.data.usecase.MessageUseCases
import com.linku.data.usecase.UserUseCases
import com.linku.domain.Authenticator
import com.linku.domain.Resource
import com.linku.domain.Strategy
import com.linku.domain.entity.Conversation
import com.linku.domain.eventOf
import com.linku.domain.service.NotificationService
import com.linku.im.Constants
import com.linku.im.extension.ifTrue
import com.linku.im.screen.BaseViewModel
import com.linku.im.screen.chat.composable.BubbleConfig
import com.linku.im.screen.chat.vo.MessageVO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val conversationUseCases: ConversationUseCases,
    private val messageUseCases: MessageUseCases,
    private val userUseCases: UserUseCases,
    private val notificationService: NotificationService,
    private val emojiUseCases: EmojiUseCases,
    private val authenticator: Authenticator
) : BaseViewModel<ChatState, ChatEvent>(ChatState()) {
    override fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.Initial -> {
                _state.value = readable.copy(
                    cid = event.cid
                )
                conversationUseCases.observeConversation(event.cid)
                    .onEach { conversation ->
                        _state.value = readable.copy(
                            title = conversation.name,
                            cid = conversation.id,
                            type = conversation.type,
                        )
                    }
                    .launchIn(viewModelScope)

                messageUseCases.observeMessages(event.cid)
                    .onEach { messages ->
                        _state.value = readable.copy(
                            messages = messages
                                .map { it.toReadable() }
                                .mapIndexedNotNull { index, message ->
                                    val next = if (index == messages.lastIndex) null
                                    else messages[index + 1]
                                    val showTimeLabel =
                                        next == null || message.timestamp - next.timestamp >= Constants.CHAT_LABEL_MIN_DURATION
                                    val isAnother = authenticator.currentUID != message.uid
                                    when (readable.type) {
                                        Conversation.Type.PM -> {
                                            MessageVO(
                                                message = message,
                                                config = BubbleConfig.PM(
                                                    sendState = message.sendState,
                                                    another = isAnother,
                                                    isShowTime = showTimeLabel
                                                )
                                            )
                                        }
                                        Conversation.Type.GROUP -> {
                                            val user = if (isAnother) userUseCases.findUser(
                                                message.uid,
                                                Strategy.Memory
                                            ) else null
                                            // The premise is that it must not be yourself,
                                            // If this message is the bottom message
                                            // or one level lower than it and this message is not sent by one person.
                                            val isShowAvatar =
                                                isAnother && (index == 0 || messages[index - 1].uid != message.uid)

                                            // The premise is that it must not be yourself,
                                            // If this message is the top message
                                            // or one level higher than it and this message is not sent by one person.
                                            val isShowName =
                                                isAnother && (index == messages.lastIndex || messages[index + 1].uid != message.uid)

                                            MessageVO(
                                                message = message,
                                                config = BubbleConfig.Multi(
                                                    sendState = message.sendState,
                                                    other = isAnother,
                                                    isShowTime = showTimeLabel,
                                                    avatarVisibility = isShowAvatar,
                                                    nameVisibility = isShowName,
                                                    name = user?.name ?: ""
                                                )
                                            )
                                        }
                                        else -> null
                                    }
                                },
                            scrollToBottomEvent = (readable.firstVisibleIndex == 0 && readable.offset == 0 && !readable.loading)
                                .ifTrue { eventOf(Unit) }
                                ?: readable.scrollToBottomEvent
                        )
                    }
                    .launchIn(viewModelScope)

                conversationUseCases.fetchConversation(event.cid).launchIn(viewModelScope)

                _state.value = readable.copy(
                    emojis = emojiUseCases.getAll()
                )
            }
            ChatEvent.SendMessage -> {
                when {
                    readable.uri == null -> sendTextMessage()
                    readable.text.text.isBlank() -> sendImageMessage()
                    else -> sendGraphicsMessage()
                }
            }
            is ChatEvent.TextChange -> onTextChange(event.text)
            is ChatEvent.OnScroll -> {
                _state.value = readable.copy(
                    firstVisibleIndex = event.index,
                    offset = event.offset
                )
            }
            ChatEvent.ReadAll -> {

            }
            is ChatEvent.OnFileUriChange -> {
                _state.value = readable.copy(
                    uri = event.uri,
                )
            }
            is ChatEvent.EmojiChange -> {
                val textFieldValue = readable.text
                val leftText = textFieldValue.text.subSequence(0, textFieldValue.selection.start)
                val rightText = textFieldValue.text.subSequence(
                    textFieldValue.selection.end,
                    textFieldValue.text.length
                )
                _state.value = readable.copy(
                    text = TextFieldValue(
                        text = "${leftText}${event.emoji}${rightText}",
                        selection = TextRange(textFieldValue.selection.start + event.emoji.length)
                    )
                )
            }
            is ChatEvent.Expanded -> _state.value = readable.copy(
                expended = event.value
            )
        }
    }

    private fun sendGraphicsMessage() {
        val cid = readable.cid
        val text = readable.text
        val uri = readable.uri ?: return
        if (text.text.isBlank()) return

        messageUseCases
            .graphicsMessage(
                cid = cid,
                text = text.text,
                uri = uri
            )
            .onEach { resource ->
                when (resource) {
                    Resource.Loading -> {
                        _state.value = readable.copy(
                            scrollToBottomEvent = eventOf(Unit),
                            uri = null,
                            text = TextFieldValue()
                        )
                    }
                    is Resource.Success -> {
                        notificationService.onEmit()
                    }
                    is Resource.Failure -> _state.value =
                        readable.copy(
                            event = eventOf(resource.message)
                        )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun onTextChange(value: TextFieldValue) {
        _state.value = readable.copy(text = value)
    }

    private fun sendTextMessage() {
        val cid = readable.cid
        val text = readable.text
        if (text.text.isBlank()) return
        viewModelScope.launch {
            messageUseCases.textMessage(cid, text.text)
                .onEach { resource ->
                    when (resource) {
                        Resource.Loading -> {
                            _state.value = readable.copy(
                                scrollToBottomEvent = eventOf(Unit),
                                text = TextFieldValue()
                            )
                        }
                        is Resource.Success -> {
                            notificationService.onEmit()
                        }
                        is Resource.Failure -> {
                            _state.value = readable.copy(event = eventOf(resource.message))
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun sendImageMessage() {
        val cid = readable.cid
        val uri = readable.uri ?: return

        messageUseCases.imageMessage(cid, uri)
            .onEach { resource ->
                when (resource) {
                    Resource.Loading -> {
                        _state.value = readable.copy(
                            scrollToBottomEvent = eventOf(Unit),
                            uri = null,
                        )
                    }
                    is Resource.Success -> {
                        notificationService.onEmit()
                    }
                    is Resource.Failure -> _state.value =
                        readable.copy(event = eventOf(resource.message))
                }
            }
            .launchIn(viewModelScope)

    }

}