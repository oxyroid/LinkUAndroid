package com.linku.im.screen.sign

import com.linku.domain.Event

data class SignState(
    val registerEvent: Event<Unit> = Event.Handled(),
    val loginEvent: Event<Unit> = Event.Handled(),
    val loading: Boolean = false,
    val error: Event<String> = Event.Handled(),
    val title: String = ""
)
