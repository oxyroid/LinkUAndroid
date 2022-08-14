package com.linku.im.screen.chat

import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.*
import com.linku.domain.Authenticator
import com.linku.domain.Resource
import com.linku.domain.Strategy
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.GraphicsMessage
import com.linku.domain.entity.ImageMessage
import com.linku.domain.entity.TextMessage
import com.linku.domain.service.NotificationService
import com.linku.im.Constants
import com.linku.im.R
import com.linku.im.screen.BaseViewModel
import com.linku.im.screen.chat.composable.BubbleConfig
import com.linku.im.screen.chat.composable.ReplyConfig
import com.linku.im.screen.chat.vo.MessageVO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val authenticator: Authenticator,
    private val applicationUseCases: ApplicationUseCases
) : BaseViewModel<ChatState, ChatEvent>(ChatState()) {
    private val _scrollEvent = MutableStateFlow(Unit)
    val scrollEvent: Flow<Unit> get() = _scrollEvent

    override fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.Initial -> initial(event)
            ChatEvent.SendMessage -> {
                when {
                    readable.uri == null -> sendTextMessage()
                    readable.textFieldValue.text.isBlank() -> sendImageMessage()
                    else -> sendGraphicsMessage()
                }
            }
            is ChatEvent.TextChange -> onTextChange(event)
            is ChatEvent.OnScroll -> {
                writable = readable.copy(
                    firstVisibleIndex = event.index,
                    offset = event.offset
                )
            }
            ChatEvent.ReadAll -> {

            }
            is ChatEvent.OnFileUriChange -> {
                writable = readable.copy(
                    uri = event.uri,
                )
            }
            is ChatEvent.EmojiChange -> {
                val textFieldValue = readable.textFieldValue
                val leftText = textFieldValue.text.subSequence(0, textFieldValue.selection.start)
                val rightText = textFieldValue.text.subSequence(
                    textFieldValue.selection.end,
                    textFieldValue.text.length
                )
                writable = readable.copy(
                    textFieldValue = TextFieldValue(
                        text = "${leftText}${event.emoji}${rightText}",
                        selection = TextRange(textFieldValue.selection.start + event.emoji.length)
                    )
                )
            }
            is ChatEvent.Expanded -> writable = readable.copy(
                expended = event.value
            )
            is ChatEvent.Reply -> {
                viewModelScope.launch {
                    writable = readable.copy(
                        repliedMessage = messageUseCases
                            .getMessage(event.mid, Strategy.OnlyCache)
                            ?.let {
                                if (it == readable.repliedMessage) null
                                else it
                            }
                    )
                }
            }
            ChatEvent.DismissImage -> writable = readable.copy(
                visitImage = ""
            )
            is ChatEvent.ShowImage -> writable = readable.copy(
                visitImage = event.img
            )
        }
    }
    private val TAG = "VM"

    private fun initial(event: ChatEvent.Initial) {
        Log.e(TAG, "initial: ")
        writable = readable.copy(
            cid = event.cid
        )
        conversationUseCases.observeConversation(event.cid)
            .onEach { conversation ->
                Log.e(TAG, "observed: ")
                writable = readable.copy(
                    title = conversation.name,
                    cid = conversation.id,
                    type = conversation.type,
                )
            }
            .launchIn(viewModelScope)

        messageUseCases.observeMessages(event.cid)
            .onEach { messages ->
                writable = readable.copy(
                    messages = messages
                        .mapIndexedNotNull { index, message ->
                            val next = if (index == messages.lastIndex) null
                            else messages[index + 1]
                            val pre = if (index == 0) null
                            else messages[index - 1]
                            val showTimeLabel =
                                next == null || message.timestamp - next.timestamp >= Constants.CHAT_LABEL_MIN_DURATION
                            val isAnother = authenticator.currentUID != message.uid
                            val reply = message.reply()
                            val repliedMessage =
                                if (reply == null) null else messageUseCases.getMessage(
                                    reply,
                                    Strategy.Memory
                                )
                            val replyConfig = if (repliedMessage == null) null
                            else ReplyConfig(
                                targetMid = reply!!,
                                index = messages.indexOfFirst { it.id == reply },
                                display = when (repliedMessage) {
                                    is TextMessage -> repliedMessage.text
                                    is ImageMessage -> applicationUseCases.getString(R.string.image_message)
                                    is GraphicsMessage -> applicationUseCases.getString(R.string.graphics_message)
                                    else -> applicationUseCases.getString(R.string.unknown_message_type)
                                }
                            )
                            when (readable.type) {
                                Conversation.Type.PM -> {
                                    MessageVO(
                                        message = message,
                                        config = BubbleConfig.PM(
                                            sendState = message.sendState,
                                            another = isAnother,
                                            isShowTime = showTimeLabel,
                                            isEndOfGroup = message.uid != pre?.uid,
                                            reply = replyConfig
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
                                            name = user?.name ?: "",
                                            avatar = user?.avatar ?: "",
                                            isEndOfGroup = message.uid != pre?.uid,
                                            reply = replyConfig
                                        )
                                    )
                                }
                                else -> null
                            }
                        }
                )
                if (readable.firstVisibleIndex == 0 && readable.offset == 0 && !readable.loading) {
                    _scrollEvent.emit(Unit)
                }
            }
            .launchIn(viewModelScope)

        conversationUseCases.fetchConversation(event.cid).launchIn(viewModelScope)

        writable = readable.copy(
            emojis = emojiUseCases.getAll()
        )
    }

    private fun sendGraphicsMessage() {
        val cid = readable.cid
        val textFieldValue = readable.textFieldValue
        val uri = readable.uri ?: return
        val reply = readable.repliedMessage?.id
        if (textFieldValue.text.isBlank()) return
        messageUseCases
            .graphicsMessage(
                cid = cid,
                text = textFieldValue.text,
                uri = uri,
                reply = reply
            )
            .onEach { resource ->
                when (resource) {
                    Resource.Loading -> {
                        _scrollEvent.emit(Unit)
                        writable = readable.copy(
                            uri = null,
                            textFieldValue = TextFieldValue(),
                            repliedMessage = null
                        )
                    }
                    is Resource.Success -> {
                        notificationService.onEmit()
                    }
                    is Resource.Failure -> {
                        onMessage(resource.message)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun onTextChange(event: ChatEvent.TextChange) {
        writable = readable.copy(textFieldValue = event.text)
    }

    private fun sendTextMessage() {
        val cid = readable.cid
        val text = readable.textFieldValue
        val reply = readable.repliedMessage?.id
        if (text.text.isBlank()) return
        viewModelScope.launch {
            messageUseCases.textMessage(cid, text.text, reply)
                .onEach { resource ->
                    when (resource) {
                        Resource.Loading -> {
                            _scrollEvent.emit(Unit)
                            writable = readable.copy(
                                textFieldValue = TextFieldValue(),
                                repliedMessage = null
                            )
                        }
                        is Resource.Success -> {
                            notificationService.onEmit()
                        }
                        is Resource.Failure -> {
                            onMessage(resource.message)
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun sendImageMessage() {
        val cid = readable.cid
        val uri = readable.uri ?: return
        val reply = readable.repliedMessage?.id
        messageUseCases.imageMessage(cid, uri, reply)
            .onEach { resource ->
                when (resource) {
                    Resource.Loading -> {
                        _scrollEvent.emit(Unit)
                        writable = readable.copy(
                            uri = null,
                            repliedMessage = null
                        )
                    }
                    is Resource.Success -> {
                        notificationService.onEmit()
                    }
                    is Resource.Failure -> {
                        onMessage(resource.message)
                    }
                }
            }
            .launchIn(viewModelScope)

    }

}