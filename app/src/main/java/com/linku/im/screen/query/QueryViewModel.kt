package com.linku.im.screen.query

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.linku.core.ktx.ifFalse
import com.linku.core.ktx.ifTrue
import com.linku.data.usecase.ConversationUseCases
import com.linku.data.usecase.MessageUseCases
import com.linku.data.usecase.UserUseCases
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QueryViewModel @Inject constructor(
    private val conversations: ConversationUseCases,
    private val userUseCases: UserUseCases,
    private val messages: MessageUseCases
) : BaseViewModel<QueryState, QueryEvent>(QueryState()) {
    override fun onEvent(event: QueryEvent) {
        when (event) {
            QueryEvent.Query -> query()
            is QueryEvent.OnText -> onText(event.text)
            QueryEvent.ToggleIncludeDescription -> toggleIncludeDescription()
            QueryEvent.ToggleIncludeEmail -> toggleIncludeEmail()
        }
    }

    private fun toggleIncludeDescription() {
        writable = readable.copy(
            isDescription = !readable.isDescription
        )
        hasQuery.ifTrue(::query)
    }

    private fun toggleIncludeEmail() {
        writable = readable.copy(
            isEmail = !readable.isEmail
        )
        hasQuery.ifTrue(::query)
    }

    private fun onText(text: TextFieldValue) {
        writable = readable.copy(
            text = text
        )
    }

    private var hasQuery: Boolean = false
    private fun query() {
        hasQuery = true
        viewModelScope.launch {
            val list = conversations.queryConversations(
                name = readable.isDescription.ifFalse { readable.text.text },
                description = readable.isDescription.ifTrue { readable.text.text }
            )
            writable = readable.copy(
                conversations = list
            )

        }
        viewModelScope.launch {
            val users = userUseCases.query(
                name = readable.isEmail.ifFalse { readable.text.text },
                email = readable.isEmail.ifTrue { readable.text.text }
            )
            writable = readable.copy(
                users = users
            )
        }

        viewModelScope.launch {
            val list = messages.queryMessages(readable.text.text)
            writable = readable.copy(
                messages = list
            )
        }

    }

}
