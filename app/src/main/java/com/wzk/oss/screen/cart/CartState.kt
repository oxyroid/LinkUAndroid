package com.wzk.oss.screen.cart

import com.wzk.domain.entity.Food
import com.wzk.wrapper.Event

data class CartState(
    val loading: Boolean = false,
    val foods: List<Food> = emptyList(),
    val error: Event<Pair<String, Int>> = Event.Handled()
)
