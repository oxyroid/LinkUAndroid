package com.linku.im.screen.main

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.linku.core.extension.ifFalse
import com.linku.core.extension.ifTrue
import com.linku.core.ktx.receiver.friendlyFormatted
import com.linku.core.ktx.receiver.withTimeContentReceiver
import com.linku.core.utils.LinkedNode
import com.linku.core.utils.forward
import com.linku.core.utils.remain
import com.linku.data.usecase.ApplicationUseCases
import com.linku.data.usecase.ConversationUseCases
import com.linku.data.usecase.MessageUseCases
import com.linku.data.usecase.UserUseCases
import com.linku.domain.auth.Authenticator
import com.linku.domain.bean.ui.ContactRequestUI
import com.linku.domain.bean.ui.ConversationUI
import com.linku.domain.bean.ui.toContactUI
import com.linku.domain.bean.ui.toUI
import com.linku.domain.entity.*
import com.linku.im.R
import com.linku.im.screen.*
import com.linku.im.vm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val conversations: ConversationUseCases,
    private val applications: ApplicationUseCases,
    private val messages: MessageUseCases,
    private val users: UserUseCases,
    private val authenticator: Authenticator
) : BaseViewModel<MainState, MainEvent>(MainState()) {

    private val _linkedNode = mutableStateOf<LinkedNode<MainMode>>(
        LinkedNode(MainMode.Conversations)
    )
    val linkedNode: State<LinkedNode<MainMode>> get() = _linkedNode

    override fun onEvent(event: MainEvent) {
        when (event) {
            MainEvent.ObserveConversations -> observeConversations()
            MainEvent.UnsubscribeConversations -> unsubscribe()
            is MainEvent.Pin -> pin(event)
            is MainEvent.Forward -> forward(event)
            MainEvent.Remain -> remain()
            MainEvent.FetchNotifications -> fetchNotifications()

            MainEvent.Query -> query()
            is MainEvent.OnQueryText -> onText(event.text)
            MainEvent.ToggleQueryIncludeDescription -> toggleIncludeDescription()
            MainEvent.ToggleQueryIncludeEmail -> toggleIncludeEmail()
        }
    }

    private fun fetchNotifications() {
        val currentUID = authenticator.currentUID
        viewModelScope.launch {
            val requests = messages
                .findMessagesByType<ContactRequest>(Message.Type.ContactRequest)
                .filter { it.tid == currentUID }
                .map { request ->
                    val user = users.findUser(request.uid)
                    val requestUI = ContactRequestUI(
                        name = user?.name.orEmpty(),
                        message = request.text,
                        url = user?.avatar.orEmpty(),
                        time = request.timestamp.withTimeContentReceiver { it.friendlyFormatted },
                        uid = request.uid
                    )
                    requestUI
                }
                .let(::ContactRequestUIList)
            writable = readable.copy(
                requests = requests
            )
        }
    }

    private fun remain() {
        _linkedNode.value = linkedNode.value.remain()
    }

    private fun forward(event: MainEvent.Forward) {
        _linkedNode.value = linkedNode.value.forward(event.mode)
    }

    private var observeConversationsJob: Job? = null
    private fun observeConversations() {
        conversations.observeConversations()
            .onEach { conversations ->
                writable = readable.copy(
                    conversations = conversations
                        .filter { it.type == Conversation.Type.GROUP }
                        .map(Conversation::toUI)
                        .sortedByDescending { it.updatedAt }
                        .let { ConversationUIList(it) },
                    contracts = conversations
                        .filter { it.type == Conversation.Type.PM }
//                            .map {
//                                val uid = conversationUseCases.convertConversationToContact(it.id)
//                                it.toContactUI(
//                                    username = uid?.let { notnull ->
//                                        users.findUser(
//                                            notnull,
//                                            Strategy.OnlyCache
//                                        )?.name.orEmpty()
//                                    }.orEmpty()
//                                )
//                            }
                        .map(Conversation::toContactUI)
                        .sortedByDescending { it.updatedAt }
                        .let { ContactUIList(it) }
                )
                observeConversationsJob?.cancel()
                observeConversationsJob = viewModelScope.launch {
                    conversations.forEach { conversation ->
                        messages.observeLatestMessage(conversation.id)
                            .onEach { message ->
                                val oldConversations = readable.conversations.value.toMutableList()
                                val oldContracts = readable.contracts.value.toMutableList()
                                val oldConversation: ConversationUI? =
                                    oldConversations.find { it.id == message.cid }
                                val oldContract = oldContracts.find { it.id == message.cid }
                                if (oldConversation != null) {
                                    oldConversations.remove(oldConversation)
                                    val copy = oldConversation.copy(
                                        content = when (message) {
                                            is TextMessage -> message.text
                                            is ImageMessage -> applications.getString(R.string.image_message)
                                            is GraphicsMessage -> applications.getString(R.string.graphics_message)
                                            else -> applications.getString(R.string.unknown_message_type)
                                        }
                                    )
                                    oldConversations.add(copy)
                                } else if (oldContract != null) {
                                    oldContracts.remove(oldContract)
                                    val copy = oldContract.copy(
                                        content = when (message) {
                                            is TextMessage -> message.text
                                            is ImageMessage -> applications.getString(R.string.image_message)
                                            is GraphicsMessage -> applications.getString(R.string.graphics_message)
                                            else -> applications.getString(R.string.unknown_message_type)
                                        }
                                    )
                                    oldContracts.add(copy)
                                } else {
                                    // TODO
                                }
                                writable = readable.copy(
                                    conversations = ConversationUIList(oldConversations),
                                    contracts = ContactUIList(oldContracts)
                                )
                            }
                            .launchIn(this)
                    }
                }
            }
            .launchIn(viewModelScope)
    }


    private fun unsubscribe() {
        observeConversationsJob?.cancel()
    }

    private fun pin(event: MainEvent.Pin) {
        viewModelScope.launch {
            conversations.pin(event.cid)
        }
    }

    private fun toggleIncludeDescription() {
        writable = readable.copy(
            queryTextIsDescription = !readable.queryTextIsDescription
        )
        hasQuery.ifTrue(::query)
    }

    private fun toggleIncludeEmail() {
        writable = readable.copy(
            queryTextIsEmail = !readable.queryTextIsEmail
        )
        hasQuery.ifTrue(::query)
    }

    private fun onText(text: TextFieldValue) {
        writable = readable.copy(
            queryText = text
        )
    }

    private var hasQuery: Boolean = false
    private fun query() {
        hasQuery = true
        viewModelScope.launch {
            val list = conversations.queryConversations(
                name = readable.queryTextIsDescription.ifFalse { readable.queryText.text },
                description = readable.queryTextIsDescription.ifTrue { readable.queryText.text }
            )
            writable = readable.copy(
                queryResultConversations = ConversationList(list)
            )

        }
        viewModelScope.launch {
            val users = users.query(
                name = readable.queryTextIsEmail.ifFalse { readable.queryText.text },
                email = readable.queryTextIsEmail.ifTrue { readable.queryText.text }
            )
            writable = readable.copy(
                queryResultUsers = UserList(users)
            )
        }

        viewModelScope.launch {
            val list = messages.queryMessages(readable.queryText.text)
            writable = readable.copy(
                queryResultMessages = MessageList(list)
            )
        }

    }
}

inline fun globalLabelOrElse(block: () -> String): String {
    return vm.readable.label ?: block()
}
