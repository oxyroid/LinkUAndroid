package com.linku.im.screen.login

import com.linku.domain.Event

data class LoginState(
    val registerEvent: Event<Unit> = Event.Handled(),
    val loginEvent: Event<Unit> = Event.Handled(),
    val loading: Boolean = false,
    val error: Event<String> = Event.Handled(),
    val title: String = ""
)
