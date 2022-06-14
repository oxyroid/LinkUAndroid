package com.wzk.oss.screen.detail

import com.wzk.domain.entity.Food
import com.wzk.wrapper.Event

data class DetailState(
    val loading: Boolean = false,
    val adding: Boolean = false,
    val addEvent: Event<Int> = Event.Handled(),
    val food: Food? = null,
    val error: Event<Pair<String, Int>> = Event.Handled()
)
