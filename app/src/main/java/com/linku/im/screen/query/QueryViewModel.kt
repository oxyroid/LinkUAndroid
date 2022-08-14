package com.linku.im.screen.query

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.ConversationUseCases
import com.linku.domain.Resource
import com.linku.im.extension.ifFalse
import com.linku.im.extension.ifTrue
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class QueryViewModel @Inject constructor(
    private val useCases: ConversationUseCases
) : BaseViewModel<QueryState, QueryEvent>(QueryState()) {
    override fun onEvent(event: QueryEvent) {
        when (event) {
            QueryEvent.Query -> query()
            is QueryEvent.OnText -> onText(event.text)
            QueryEvent.ToggleIncludeDescription -> toggleIncludeDescription()
        }
    }

    private fun toggleIncludeDescription() {
        writable = readable.copy(
            isDescription = !readable.isDescription
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
        useCases.queryConversations(
            name = readable.isDescription.ifFalse { readable.text.text },
            description = readable.isDescription.ifTrue { readable.text.text }
        )
            .onEach { resource ->
                writable = when (resource) {
                    Resource.Loading -> readable.copy(
                        querying = true
                    )
                    is Resource.Success -> readable.copy(
                        querying = false,
                        conversations = resource.data
                    )
                    is Resource.Failure -> {
                        onMessage(resource.message)
                        readable.copy(
                            querying = false
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

}