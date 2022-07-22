package com.linku.im.screen.query

import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.ConversationUseCases
import com.linku.domain.Resource
import com.linku.domain.eventOf
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
        _state.value = readableState.copy(
            includeDescription = !readableState.includeDescription
        )
        hasQuery.ifTrue { query() }
    }

    private fun onText(text: String) {
        _state.value = readableState.copy(
            text = text
        )
    }

    private var hasQuery: Boolean = false
    private fun query() {
        hasQuery = true
        useCases.queryConversations(
            name = readableState.includeDescription.ifFalse { readableState.text },
            description = readableState.includeDescription.ifTrue { readableState.text }
        )
            .onEach { resource ->
                _state.value = when (resource) {
                    Resource.Loading -> readableState.copy(
                        querying = true
                    )
                    is Resource.Success -> readableState.copy(
                        querying = false,
                        conversations = resource.data
                    )
                    is Resource.Failure -> readableState.copy(
                        querying = false,
                        message = eventOf(resource.message)
                    )
                }
            }
            .launchIn(viewModelScope)
    }

}