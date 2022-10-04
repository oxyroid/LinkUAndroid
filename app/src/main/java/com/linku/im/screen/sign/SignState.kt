package com.linku.im.screen.sign

import androidx.compose.ui.text.input.TextFieldValue
import com.linku.domain.Event

data class SignState(
    val loginEvent: Event<Unit> = Event.Handled(),
    val loading: Boolean = false,
    val syncingPercent: Int? = null,
    val email: TextFieldValue = TextFieldValue(),
    val password: TextFieldValue = TextFieldValue()
)
