package com.linku.im.screen.sign

import com.linku.domain.Event

data class SignState(
    val registerEvent: Event<Unit> = Event.Handled(),
    val loginEvent: Event<Unit> = Event.Handled(),
    val loading: Boolean = false,
    val email: String = "",
    val password: String = ""
)
