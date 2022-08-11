package com.linku.im.screen.preview

import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.MessageUseCases
import com.linku.domain.Resource
import com.linku.domain.entity.GraphicsMessage
import com.linku.domain.entity.ImageMessage
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val messageUseCases: MessageUseCases
) : BaseViewModel<PreviewState, PreviewEvent>(PreviewState()) {
    override fun onEvent(event: PreviewEvent) {
        when (event) {
            is PreviewEvent.GetImageUrl -> {
                messageUseCases.getMessage(event.mid)
                    .onEach { resource ->
                        writable = when (resource) {
                            Resource.Loading -> readable
                            is Resource.Success -> readable.copy(
                                url = when (val message = resource.data) {
                                    is ImageMessage -> message.url
                                    is GraphicsMessage -> message.url
                                    else -> ""
                                }
                            )
                            is Resource.Failure -> readable
                        }
                    }
                    .launchIn(viewModelScope)
            }
        }
    }
}