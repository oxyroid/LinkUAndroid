package com.linku.im.screen.chat

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.ApplicationUseCases
import com.linku.data.usecase.ConversationUseCases
import com.linku.data.usecase.EmojiUseCases
import com.linku.data.usecase.MessageUseCases
import com.linku.data.usecase.UserUseCases
import com.linku.domain.Strategy
import com.linku.domain.auth.Authenticator
import com.linku.domain.bean.Bubble
import com.linku.domain.bean.Reply
import com.linku.domain.bean.ui.MessageUI
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.GraphicsMessage
import com.linku.domain.entity.ImageMessage
import com.linku.domain.entity.Message
import com.linku.domain.entity.TextMessage
import com.linku.domain.service.NotificationService
import com.linku.domain.util.LinkedNode
import com.linku.domain.util.forward
import com.linku.domain.util.remain
import com.linku.domain.util.remainIf
import com.linku.domain.wrapper.Resource
import com.linku.domain.wrapper.eventOf
import com.linku.im.Constants
import com.linku.im.R
import com.linku.im.ktx.dsl.suggestAny
import com.linku.im.ktx.receiver.isSameDay
import com.linku.im.ktx.receiver.isToday
import com.linku.im.ktx.receiver.withTimeContentReceiver
import com.linku.im.screen.BaseViewModel
import com.linku.im.screen.chat.ChatEvent.CancelMessage
import com.linku.im.screen.chat.ChatEvent.FetchChannel
import com.linku.im.screen.chat.ChatEvent.FetchChannelDetail
import com.linku.im.screen.chat.ChatEvent.Forward
import com.linku.im.screen.chat.ChatEvent.ObserveMessage
import com.linku.im.screen.chat.ChatEvent.OnEmoji
import com.linku.im.screen.chat.ChatEvent.OnEmojiSpanExpanded
import com.linku.im.screen.chat.ChatEvent.OnFile
import com.linku.im.screen.chat.ChatEvent.OnFocus
import com.linku.im.screen.chat.ChatEvent.OnReply
import com.linku.im.screen.chat.ChatEvent.OnTextChange
import com.linku.im.screen.chat.ChatEvent.PushShortcut
import com.linku.im.screen.chat.ChatEvent.ReadAll
import com.linku.im.screen.chat.ChatEvent.Remain
import com.linku.im.screen.chat.ChatEvent.RemainIf
import com.linku.im.screen.chat.ChatEvent.ResendMessage
import com.linku.im.screen.chat.ChatEvent.SendMessage
import com.linku.im.screen.chat.vo.MemberVO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val conversations: ConversationUseCases,
    private val messages: MessageUseCases,
    private val users: UserUseCases,
    private val notifications: NotificationService,
    private val emojis: EmojiUseCases,
    private val authenticator: Authenticator,
    private val applications: ApplicationUseCases
) : BaseViewModel<ChatState, ChatEvent>(ChatState()) {
    private val _messageFlow = MutableStateFlow(emptyList<MessageUI>())
    val messageFlow: SharedFlow<List<MessageUI>> = _messageFlow

    private val _memberFlow = MutableStateFlow(emptyList<MemberVO>())
    val memberFlow: SharedFlow<List<MemberVO>> = _memberFlow

    private val _linkedNode =
        mutableStateOf<LinkedNode<ChatMode>>(LinkedNode(ChatMode.Messages))
    val linkedNode: State<LinkedNode<ChatMode>> get() = _linkedNode

    override fun onEvent(event: ChatEvent) = when (event) {
        is FetchChannel -> fetchChannel(event)
        FetchChannelDetail -> fetchChannelDetail()
        ObserveMessage -> observeMessages()
        PushShortcut -> pushShortcut()
        is OnTextChange -> onTextChange(event)
        is OnEmoji -> onEmoji(event)
        is OnFile -> onFile(event)
        is OnReply -> onReply(event)
        is OnFocus -> onFocus(event)
        is OnEmojiSpanExpanded -> onEmojiSpanExpanded(event)
        SendMessage -> sendMessage()
        is ResendMessage -> resendMessage(event)
        is CancelMessage -> TODO()
        ReadAll -> TODO()
        is Forward -> forward(event)
        Remain -> remain()
        is RemainIf -> remainIf(event)
    }

    private fun resendMessage(event: ResendMessage) {
        viewModelScope.launch {
            messages
                .resendMessage(event.mid)
                .launchIn(this)
        }
    }

    private var observeChannelJob: Job? = null
    private fun fetchChannel(event: FetchChannel) {
        writable = readable.copy(
            cid = event.cid,
            emojis = emojis.getAll()
        )
        viewModelScope.launch {
            _messageFlow.emit(emptyList())
        }
        observeChannelJob?.cancel()
        observeChannelJob = conversations.observeConversation(event.cid)
            .onEach { conversation ->
                writable = readable.copy(
                    title = conversation.name,
                    subTitle = when (conversation.type) {
                        Conversation.Type.BANNED -> R.string.channel_type_banned
                        Conversation.Type.GROUP -> R.string.channel_type_group
                        Conversation.Type.PM -> R.string.channel_type_pm
                        Conversation.Type.UNKNOWN -> R.string.channel_type_unknown
                    }.let(applications.getString::invoke),
                    introduce = conversation.description,
                    cid = conversation.id,
                    type = conversation.type
                )
            }
            .launchIn(viewModelScope)
    }

    private fun observeMessages() {
        observeMessagesJob?.cancel()
        observeMessagesJob = messages.observeMessages(readable.cid)
            .onEach { messages ->
                mapMessagesJob?.cancel()
                mapMessagesJob = viewModelScope.launch {
                    messages
                        .mapIndexedNotNull { index, m -> messages.calculate(index, m) }
                        .also {
                            _messageFlow.emit(it)
                        }
                    writable = readable.copy(
                        scroll = eventOf(0)
                    )

                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun List<Message>.calculate(index: Int, message: Message): MessageUI? {
        val next = getOrNull(index + 1)
        val pre = getOrNull(index - 1)
        val isAnother = authenticator.currentUID != message.uid
        val isShowTime = com.linku.im.ktx.dsl.any {
            suggest { next == null }
            suggest {
                checkNotNull(next)
                message.timestamp.withTimeContentReceiver {
                    if (it.isToday) it - next.timestamp >= Constants.CHAT_LABEL_MIN_DURATION
                    else !it.isSameDay(next.timestamp)
                }
            }
        }
        val repliedMid = message.reply()
        val repliedMessage = repliedMid?.let {
            messages.getMessage(it, Strategy.Memory)
        }
        val reply = repliedMessage?.let { m ->
            Reply(
                repliedMid = repliedMid,
                index = indexOfFirst { it.id == repliedMid },
                display = when (m) {
                    is TextMessage -> m.text
                    is ImageMessage -> applications.getString(R.string.image_message)
                    is GraphicsMessage -> applications.getString(R.string.graphics_message)
                    else -> applications.getString(R.string.unknown_message_type)
                }
            )
        }
        return when (readable.type) {
            Conversation.Type.PM -> {
                MessageUI(
                    message = message,
                    config = Bubble.PM(
                        sendState = message.sendState,
                        another = isAnother,
                        isShowTime = isShowTime,
                        isEndOfGroup = message.uid != pre?.uid,
                        reply = reply
                    )
                )
            }

            Conversation.Type.GROUP -> {
                val user = if (isAnother) users.findUser(
                    message.uid,
                    Strategy.Memory
                ) else null

                MessageUI(
                    message = message,
                    config = Bubble.Group(
                        sendState = message.sendState,
                        other = isAnother,
                        isShowTime = isShowTime,
                        avatarVisibility = com.linku.im.ktx.dsl.all {
                            suggest { isAnother }
                            suggestAny {
                                it.suggest { index == 0 }
                                it.suggest { get(index - 1).uid != message.uid }
                            }
                        },
                        nameVisibility = com.linku.im.ktx.dsl.all {
                            suggest { isAnother }
                            suggestAny {
                                it.suggest { index == lastIndex }
                                it.suggest { get(index + 1).uid != message.uid }
                            }
                        },
                        name = user?.name ?: "",
                        avatar = user?.avatar ?: "",
                        isEndOfGroup = message.uid != pre?.uid,
                        reply = reply
                    )
                )
            }

            else -> null
        }

    }

    override fun restore() {
        super.restore()
        observeChannelJob?.cancel()
        observeMessagesJob?.cancel()
        mapMessagesJob?.cancel()
    }

    private var observeMessagesJob: Job? = null
    private var mapMessagesJob: Job? = null

    private fun fetchChannelDetail() {
        conversations.fetchMembers(readable.cid)
            .onEach { resource ->
                writable = when (resource) {
                    Resource.Loading -> {
                        _memberFlow.emit(emptyList())
                        readable.copy(
                            channelDetailLoading = true
                        )
                    }

                    is Resource.Success -> {
                        resource.data
                            .map {
                                val user = users.findUser(it.uid, Strategy.CacheElseNetwork)
                                MemberVO(
                                    cid = it.cid,
                                    uid = it.uid,
                                    username = user?.name.orEmpty(),
                                    avatar = user?.avatar.orEmpty(),
                                    admin = it.root,
                                    memberName = it.name.orEmpty()
                                )
                            }
                            .sortedByDescending { it.admin }
                            .also {
                                _memberFlow.emit(it)
                            }

                        readable.copy(
                            channelDetailLoading = false
                        )
                    }

                    is Resource.Failure -> readable.copy(
                        channelDetailLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun pushShortcut() {
        conversations.pushConversationShort(readable.cid)
            .onEach { resource ->
                writable = when (resource) {
                    Resource.Loading -> readable.copy(
                        shortcutPushing = true
                    )

                    is Resource.Success -> readable.copy(
                        shortcutPushing = false,
                        shortcutPushed = true
                    )

                    is Resource.Failure -> {
                        onMessage(resource.message)
                        readable.copy(
                            shortcutPushing = false,
                            shortcutPushed = false
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun onTextChange(event: OnTextChange) {
        writable = readable.copy(textFieldValue = event.text)
    }

    private fun onEmoji(event: OnEmoji) {
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

    private fun onFile(event: OnFile) {
        writable = readable.copy(
            uri = event.uri
        )
    }

    private fun onReply(event: OnReply) {
        viewModelScope.launch {
            writable = readable.copy(
                repliedMessage = event.mid?.let {
                    messages
                        .getMessage(it, Strategy.OnlyCache)
                        ?.let { message ->
                            if (message == readable.repliedMessage) null
                            else message
                        }

                }
            )
        }
    }

    private fun onFocus(event: OnFocus) {
        writable = readable.copy(
            focusMessageId = event.mid
        )
    }

    private fun onEmojiSpanExpanded(event: OnEmojiSpanExpanded) {
        writable = readable.copy(
            emojiSpanExpanded = event.value
        )
    }

    private fun sendMessage() {
        val isUriEmpty = readable.uri == null
        val isTextEmpty = readable.textFieldValue.text.isBlank()
        when {
            isUriEmpty && !isTextEmpty -> sendTextMessage()
            !isUriEmpty && isTextEmpty -> sendImageMessage()
            !isUriEmpty && !isTextEmpty -> sendGraphicsMessage()
        }
    }

    private fun sendTextMessage() {
        val cid = readable.cid
        val text = readable.textFieldValue
        val reply = readable.repliedMessage?.id
        if (text.text.isBlank()) return
        viewModelScope.launch {
            messages.textMessage(cid, text.text, reply)
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
                            notifications.onEmit()
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
        messages.imageMessage(cid, uri, reply)
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
                        notifications.onEmit()
                    }

                    is Resource.Failure -> {
                        onMessage(resource.message)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun sendGraphicsMessage() {
        val cid = readable.cid
        val textFieldValue = readable.textFieldValue
        val uri = readable.uri ?: return
        val reply = readable.repliedMessage?.id
        if (textFieldValue.text.isBlank()) return
        messages
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
                        notifications.onEmit()
                    }

                    is Resource.Failure -> {
                        onMessage(resource.message)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun forward(event: Forward) {
        _linkedNode.value = linkedNode.value.forward(event.mode)
    }

    private fun remain() {
        _linkedNode.value = linkedNode.value.remain()
    }

    private fun remainIf(event: RemainIf) {
        _linkedNode.value = linkedNode.value.remainIf { event.block() }
    }
}
