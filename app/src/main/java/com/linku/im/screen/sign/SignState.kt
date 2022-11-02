package com.linku.im.screen.sign

import androidx.compose.ui.text.input.TextFieldValue
import com.linku.core.wrapper.Event

data class SignState(
    val loginEvent: Event<Unit> = Event.Handled(),
    val loading: Boolean = false,
    val email: TextFieldValue = TextFieldValue(),
    val password: TextFieldValue = TextFieldValue()
)
