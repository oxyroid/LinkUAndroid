package com.wzk.oss.screen.list

import com.wzk.domain.entity.Food
import com.wzk.wrapper.Event

data class ListState(
    val loading: Boolean = false,
    val list: List<Food> = emptyList(),
    val error: Event<Pair<String, Int>> = Event.Handled()
)