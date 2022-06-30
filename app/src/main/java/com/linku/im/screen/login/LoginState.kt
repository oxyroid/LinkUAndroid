package com.linku.im.screen.login

import com.linku.domain.entity.User
import com.linku.wrapper.Event

data class LoginState(
    val registerEvent: Event<User> = Event.Handled(),
    val loginEvent: Event<User> = Event.Handled(),
    val loading: Boolean = false,
    val error: Event<String> = Event.Handled()
)
