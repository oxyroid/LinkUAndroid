package com.wzk.oss.screen.list

import androidx.lifecycle.viewModelScope
import com.wzk.domain.usecase.FoodUseCases
import com.wzk.oss.screen.BaseViewModel
import com.wzk.oss.screen.OrderType
import com.wzk.wrapper.Resource
import com.wzk.wrapper.eventOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val foodUseCases: FoodUseCases
) : BaseViewModel<ListState, ListEvent>(ListState()) {

    init {
        onEvent(ListEvent.Fetch(ListOrder.Name()))
    }

    override fun onEvent(event: ListEvent) {
        when (event) {
            is ListEvent.Fetch -> {
                foodUseCases.fetchListUseCase().onEach { resource ->
                    _state.value = when (resource) {
                        Resource.Loading -> ListState(loading = true)
                        is Resource.Success -> ListState(
                            loading = false,
                            list = when (val order = event.listOrder) {
                                is ListOrder.Name -> {
                                    when (order.orderType) {
                                        OrderType.Ascending -> resource.data.sortedBy { it.name }
                                        OrderType.Descending -> resource.data.sortedByDescending { it.name }
                                    }
                                }
                                is ListOrder.Price -> {
                                    when (order.orderType) {
                                        OrderType.Ascending -> resource.data.sortedBy { it.price }
                                        OrderType.Descending -> resource.data.sortedByDescending { it.price }
                                    }
                                }
                            }

                        )
                        is Resource.Failure -> ListState(
                            error = eventOf(resource.message to resource.code)
                        )
                    }
                }.launchIn(viewModelScope)
            }
            is ListEvent.Order -> {
                val oldList = _state.value.list
                _state.value = _state.value.copy(
                    list = when (event.listOrder) {
                        is ListOrder.Name -> when (event.listOrder.orderType) {
                            OrderType.Ascending -> oldList.sortedBy { it.name }
                            OrderType.Descending -> oldList.sortedByDescending { it.name }
                        }
                        is ListOrder.Price -> when (event.listOrder.orderType) {
                            OrderType.Ascending -> oldList.sortedBy { it.price }
                            OrderType.Descending -> oldList.sortedByDescending { it.price }
                        }
                    }
                )
            }
            else -> {}
        }
    }
}