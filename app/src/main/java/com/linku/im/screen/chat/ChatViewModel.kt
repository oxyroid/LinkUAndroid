package com.linku.im.screen.chat

import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.linku.data.debug
import com.linku.data.usecase.*
import com.linku.domain.Authenticator
import com.linku.domain.Resource
import com.linku.domain.Strategy
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.GraphicsMessage
import com.linku.domain.entity.ImageMessage
import com.linku.domain.entity.TextMessage
import com.linku.domain.eventOf
import com.linku.domain.service.NotificationService
import com.linku.domain.struct.LinkedNode
import com.linku.domain.struct.hasNext
import com.linku.im.Constants
import com.linku.im.R
import com.linku.im.extension.isSameDay
import com.linku.im.extension.isToday
import com.linku.im.screen.BaseViewModel
import com.linku.im.screen.chat.composable.BubbleConfig
import com.linku.im.screen.chat.composable.ReplyConfig
import com.linku.im.screen.chat.vo.MessageVO
import com.thxbrop.suggester.all
import com.thxbrop.suggester.any
import com.thxbrop.suggester.suggestAny
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

    private var syncingMessagesJob: Job? = null
    private var syncingTime: Int = 0
    override fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.Initialize -> initial(event)
            is ChatEvent.OnTextChange -> onTextChange(event)
            is ChatEvent.OnScroll -> onScroll(event)
            is ChatEvent.OnFile -> onFile(event)
            is ChatEvent.OnEmoji -> onEmoji(event)
            is ChatEvent.OnExpanded -> onExpanded(event)
            is ChatEvent.OnReply -> onReply(event)
            is ChatEvent.OnPreview -> onPreview(event)
            is ChatEvent.OnFocus -> onFocus(event)
            is ChatEvent.ResendMessage -> TODO()
            is ChatEvent.CancelMessage -> TODO()
            ChatEvent.ReadAll -> TODO()
            ChatEvent.SendMessage -> sendMessage()
            ChatEvent.Syncing -> syncing()
        }
    }

    private fun syncing() {
        messageUseCases.observeMessages(readable.cid)
            .onEach { messages ->
                syncingMessagesJob?.cancel()
                debug {
                    syncingTime++
                    Log.e("CVM", "syncing:$syncingTime ")
                }
                syncingMessagesJob = viewModelScope.launch {
                    writable = readable.copy(
                        messages = messages
                            .mapIndexedNotNull { index, message ->
                                val next = if (index == messages.lastIndex) null
                                else messages[index + 1]
                                val pre = if (index == 0) null
                                else messages[index - 1]
                                val isShowTime = any {
                                    suggest { next == null }
                                    suggest {
                                        checkNotNull(next)
                                        if (message.timestamp.isToday) message.timestamp - next.timestamp >=
                                                Constants.CHAT_LABEL_MIN_DURATION
                                        else !message.timestamp.isSameDay(next.timestamp)
                                    }
                                }
                                val isAnother = authenticator.currentUID != message.uid
                                val repliedMid = message.reply()
                                val repliedMessage =
                                    if (repliedMid == null) null else messageUseCases.getMessage(
                                        repliedMid,
                                        Strategy.Memory
                                    )
                                val replyConfig = if (repliedMessage == null) null
                                else ReplyConfig(
                                    targetMid = repliedMid!!,
                                    index = messages.indexOfFirst { it.id == repliedMid },
                                    display = when (repliedMessage) {
                                        is TextMessage -> repliedMessage.text
                                        is ImageMessage -> applicationUseCases.getString(R.string.image_message)
                                        is GraphicsMessage -> applicationUseCases.getString(
                                            R.string.graphics_message
                                        )
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
                                                isShowTime = isShowTime,
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
                                        // [isShowTime] property
                                        // The premise is that it must not be yourself,
                                        // If this message is the bottom message
                                        // or one level lower than it and this message is not sent by one person.

                                        // [isShowAvatar] property
                                        // The premise is that it must not be yourself,
                                        // If this message is the top message
                                        // or one level higher than it and this message is not sent by one person.

                                        MessageVO(
                                            message = message,
                                            config = BubbleConfig.Group(
                                                sendState = message.sendState,
                                                other = isAnother,
                                                isShowTime = isShowTime,
                                                avatarVisibility = all {
                                                    suggest { isAnother }
                                                    suggestAny {
                                                        it.suggest { index == 0 }
                                                        it.suggest { messages[index - 1].uid != message.uid }
                                                    }
                                                },
                                                nameVisibility = all {
                                                    suggest { isAnother }
                                                    suggestAny {
                                                        it.suggest { index == messages.lastIndex }
                                                        it.suggest { messages[index + 1].uid != message.uid }
                                                    }
                                                },
                                                name = user?.name ?: "",
                                                avatar = user?.avatar ?: "",
                                                isEndOfGroup = message.uid != pre?.uid,
                                                reply = replyConfig
                                            )
                                        )
                                    }
                                    else -> null
                                }
                            },
                        scroll = if (readable.firstVisibleIndex == 0 && readable.offset == 0)
                            eventOf(0) else readable.scroll
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun onFocus(event: ChatEvent.OnFocus) {
        writable = readable.copy(
            focusMessageId = event.mid
        )
    }

    private fun onPreview(event: ChatEvent.OnPreview) {
        writable = readable.copy(
            preview = event.preview
        )
    }

    private fun onReply(event: ChatEvent.OnReply) {
        viewModelScope.launch {
            writable = readable.copy(
                repliedMessage = event.mid?.let {
                    messageUseCases
                        .getMessage(it, Strategy.OnlyCache)
                        ?.let { message ->
                            if (message == readable.repliedMessage) null
                            else message
                        }

                }
            )
        }
    }

    private fun onExpanded(event: ChatEvent.OnExpanded) {
        writable = readable.copy(
            expended = event.value
        )
    }

    private fun onEmoji(event: ChatEvent.OnEmoji) {
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

    private fun onFile(event: ChatEvent.OnFile) {
        writable = readable.copy(
            uri = event.uri,
        )
    }

    private fun onScroll(event: ChatEvent.OnScroll) {
        writable = readable.copy(
            firstVisibleIndex = event.index,
            offset = event.offset
        )
    }

    private fun sendMessage() {
        when {
            readable.uri == null -> sendTextMessage()
            readable.textFieldValue.text.isBlank() -> sendImageMessage()
            else -> sendGraphicsMessage()
        }
    }

    private fun initial(event: ChatEvent.Initialize) {
        writable = readable.copy(
            cid = event.cid
        )
        conversationUseCases.observeConversation(event.cid)
            .onEach { conversation ->
                writable = readable.copy(
                    title = conversation.name,
                    cid = conversation.id,
                    type = conversation.type
                )
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
                        writable = readable.copy(
                            uri = null,
                            textFieldValue = TextFieldValue(),
                            repliedMessage = null,
                            scroll = eventOf(0)
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

    private fun onTextChange(event: ChatEvent.OnTextChange) {
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
                            writable = readable.copy(
                                textFieldValue = TextFieldValue(),
                                repliedMessage = null,
                                scroll = eventOf(0)
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
                        writable = readable.copy(
                            uri = null,
                            repliedMessage = null,
                            scroll = eventOf(0)
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