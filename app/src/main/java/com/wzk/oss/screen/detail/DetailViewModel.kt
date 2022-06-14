package com.wzk.oss.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.wzk.domain.common.Constants
import com.wzk.domain.usecase.FoodUseCases
import com.wzk.oss.screen.BaseViewModel
import com.wzk.wrapper.Resource
import com.wzk.wrapper.eventOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val foodUseCases: FoodUseCases,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<DetailState, DetailEvent>(DetailState()) {

    init {
        savedStateHandle.get<String>(Constants.PARAM_FOOD_ID)?.let { foodId ->
            onEvent(DetailEvent.Find(foodId.toInt()))
        }
    }

    override fun onEvent(event: DetailEvent) {
        when (event) {
            is DetailEvent.Find -> {
                foodUseCases.findUseCase(event.id).onEach { resource ->
                    _state.value = when (resource) {
                        Resource.Loading -> _state.value.copy(loading = true)
                        is Resource.Success -> _state.value.copy(
                            loading = false,
                            food = resource.data
                        )
                        is Resource.Failure -> _state.value.copy(
                            loading = false,
                            error = eventOf(resource.message to resource.code)
                        )
                    }
                }.launchIn(viewModelScope)
            }
            is DetailEvent.AddToCart -> {
                foodUseCases.addToCartUseCase(event.id).onEach { resource ->
                    _state.value = when (resource) {
                        Resource.Loading -> _state.value.copy(adding = true)
                        is Resource.Success -> _state.value.copy(
                            adding = false,
                            addEvent = eventOf(resource.data)
                        )
                        is Resource.Failure -> _state.value.copy(
                            adding = false,
                            error = eventOf(resource.message to resource.code)
                        )
                    }
                }.launchIn(viewModelScope)
            }
        }
    }
}