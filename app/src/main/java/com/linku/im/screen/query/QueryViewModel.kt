package com.linku.im.screen.query

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.ConversationUseCases
import com.linku.data.usecase.UserUseCases
import com.linku.domain.wrapper.Resource
import com.linku.im.ktx.ifFalse
import com.linku.im.ktx.ifTrue
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QueryViewModel @Inject constructor(
    private val conversationUseCases: ConversationUseCases,
    private val userUseCases: UserUseCases
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
        conversationUseCases.queryConversations(
            name = readable.isDescription.ifFalse { readable.text.text },
            description = readable.isDescription.ifTrue { readable.text.text }
        )
            .onEach { resource ->
                writable = when (resource) {
                    Resource.Loading -> readable.copy(
                        queryingConversations = true
                    )

                    is Resource.Success -> readable.copy(
                        queryingConversations = false,
                        conversations = resource.data
                    )

                    is Resource.Failure -> {
                        onMessage(resource.message)
                        readable.copy(
                            queryingConversations = false
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
        viewModelScope.launch {
            writable = readable.copy(
                queryingUsers = true
            )
            val users = userUseCases.query(
                name = readable.isEmail.ifFalse { readable.text.text },
                email = readable.isEmail.ifTrue { readable.text.text }
            )
            writable = readable.copy(
                queryingUsers = false,
                users = users
            )
        }
    }

}
