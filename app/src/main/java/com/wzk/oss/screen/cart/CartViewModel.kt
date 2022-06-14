package com.wzk.oss.screen.cart

import androidx.lifecycle.viewModelScope
import com.wzk.domain.usecase.FoodUseCases
import com.wzk.oss.screen.BaseViewModel
import com.wzk.wrapper.Resource
import com.wzk.wrapper.eventOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val foodUseCases: FoodUseCases
) : BaseViewModel<CartState, CartEvent>(CartState()) {
    init {
        onEvent(CartEvent.LoadAll)
    }

    override fun onEvent(event: CartEvent) {
        when (event) {
            CartEvent.LoadAll -> {
                viewModelScope.launch {
                    foodUseCases.loadCartUseCase().collectLatest { resource ->
                        _state.value = when (resource) {
                            Resource.Loading -> CartState(loading = true)
                            is Resource.Success -> CartState(foods = resource.data)
                            is Resource.Failure -> CartState(
                                error = eventOf(resource.message to resource.code)
                            )
                        }
                    }
                }
            }
        }
    }
}