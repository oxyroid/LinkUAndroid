package com.wzk.oss.screen.login

import com.wzk.domain.entity.User
import com.wzk.wrapper.Event

data class LoginState(
    val registerEvent: Event<User> = Event.Handled(),
    val loginEvent: Event<User> = Event.Handled(),
    val loading: Boolean = false,
    val error: Event<String> = Event.Handled()
)
