package com.linku.im.screen.preview

import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.MessageUseCases
import com.linku.domain.Strategy
import com.linku.domain.entity.GraphicsMessage
import com.linku.domain.entity.ImageMessage
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val messageUseCases: MessageUseCases
) : BaseViewModel<PreviewState, PreviewEvent>(PreviewState()) {
    override fun onEvent(event: PreviewEvent) {
        when (event) {
            is PreviewEvent.GetImageUrl -> {
                viewModelScope.launch {
                    val latest = messageUseCases.getMessage(event.mid, Strategy.CacheElseNetwork)
                    writable = readable.copy(
                        url = when (latest) {
                            is ImageMessage -> latest.url
                            is GraphicsMessage -> latest.url
                            else -> ""
                        }
                    )
                }
            }
        }
    }
}