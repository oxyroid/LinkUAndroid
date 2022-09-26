package com.linku.im.screen.chat

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
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
import com.linku.domain.eventOf
import com.linku.domain.service.NotificationService
import com.linku.domain.struct.node.LinkedNode
import com.linku.domain.struct.node.forward
import com.linku.domain.struct.node.remain
import com.linku.domain.struct.node.remainIf
import com.linku.im.Constants
import com.linku.im.R
import com.linku.im.ktx.dsl.all
import com.linku.im.ktx.dsl.any
import com.linku.im.ktx.dsl.suggestAny
import com.linku.im.ktx.isSameDay
import com.linku.im.ktx.isToday
import com.linku.im.screen.BaseViewModel
import com.linku.im.screen.chat.ChatEvent.*
import com.linku.im.screen.chat.composable.Bubble
import com.linku.im.screen.chat.composable.Reply
import com.linku.im.screen.chat.vo.MemberVO
import com.linku.im.screen.chat.vo.MessageVO
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
    emojis: EmojiUseCases,
    private val authenticator: Authenticator,
    private val applications: ApplicationUseCases
) : BaseViewModel<ChatState, ChatEvent>(ChatState()) {
    private val _messageFlow = MutableStateFlow(emptyList<MessageVO>())
    val messageFlow: SharedFlow<List<MessageVO>> = _messageFlow

    private val _memberFlow = MutableStateFlow(emptyList<MemberVO>())
    val memberFlow: SharedFlow<List<MemberVO>> = _memberFlow

    private val _linkedNode =
        mutableStateOf<LinkedNode<ChatScreenMode>>(LinkedNode(ChatScreenMode.Messages))
    val linkedNode: State<LinkedNode<ChatScreenMode>> get() = _linkedNode

    init {
        writable = readable.copy(
            emojis = emojis.getAll()
        )
    }

    override fun onEvent(event: ChatEvent) = when (event) {
        is ObserveChannel -> observeChannel(event)
        ObserveMessage -> observeMessages()
        RemoveAllObservers -> removeAllObservers()
        is Fetch -> fetch()
        FetchChannelDetail -> fetchChannelDetail()
        PushShortcut -> pushShortcut()
        is OnTextChange -> onTextChange(event)
        is OnEmoji -> onEmoji(event)
        is OnFile -> onFile(event)
        is OnScroll -> onScroll(event)
        is OnReply -> onReply(event)
        is OnFocus -> onFocus(event)
        is OnEmojiSpanExpanded -> onEmojiSpanExpanded(event)
        SendMessage -> sendMessage()
        is ResendMessage -> TODO()
        is CancelMessage -> TODO()
        ReadAll -> TODO()
        is Forward -> forward(event)
        Remain -> remain()
        is RemainIf -> remainIf(event)
    }

    private var observeChannelJob: Job? = null
    private fun observeChannel(event: ObserveChannel) {
        writable = readable.copy(cid = event.cid)
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
                    messages.mapIndexedNotNull { index, message ->
                        val next = if (index == messages.lastIndex) null
                        else messages[index + 1]
                        val pre = if (index == 0) null
                        else messages[index - 1]
                        val isAnother = authenticator.currentUID != message.uid
                        val isShowTime = any {
                            suggest { next == null }
                            suggest {
                                checkNotNull(next)
                                if (message.timestamp.isToday) message.timestamp - next.timestamp >=
                                        Constants.CHAT_LABEL_MIN_DURATION
                                else !message.timestamp.isSameDay(next.timestamp)
                            }
                        }
                        val repliedMid = message.reply()
                        val repliedMessage = repliedMid?.let {
                            this@ChatViewModel.messages.getMessage(it, Strategy.Memory)
                        }
                        val reply = repliedMessage?.let { m ->
                            Reply(
                                repliedMid = repliedMid,
                                index = messages.indexOfFirst { it.id == repliedMid },
                                display = when (m) {
                                    is TextMessage -> m.text
                                    is ImageMessage -> applications.getString(R.string.image_message)
                                    is GraphicsMessage -> applications.getString(
                                        R.string.graphics_message
                                    )
                                    else -> applications.getString(R.string.unknown_message_type)
                                }
                            )
                        }
                        when (readable.type) {
                            Conversation.Type.PM -> {
                                MessageVO(
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
                                    config = Bubble.Group(
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
                                        reply = reply
                                    )
                                )
                            }
                            else -> null
                        }

                    }.also {
                        _messageFlow.emit(it)
                    }
                    writable = readable.copy(
                        scroll = if (readable.firstVisibleIndex == 0 && readable.firstVisibleItemScrollOffset == 0)
                            eventOf(0) else readable.scroll
                    )

                }
            }
            .launchIn(viewModelScope)
    }

    private var fetchJob: Job? = null
    private fun fetch() {
        fetchJob?.cancel()
        fetchJob = conversations.fetchConversation(readable.cid).launchIn(viewModelScope)
    }


    private fun removeAllObservers() {
        observeChannelJob?.cancel()
        observeMessagesJob?.cancel()
        mapMessagesJob?.cancel()
        fetchJob?.cancel()
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

    private fun onScroll(event: OnScroll) {
        writable = readable.copy(
            firstVisibleIndex = event.index,
            firstVisibleItemScrollOffset = event.offset
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