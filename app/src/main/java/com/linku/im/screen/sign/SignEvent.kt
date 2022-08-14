package com.linku.im.screen.sign

import androidx.compose.ui.text.input.TextFieldValue

sealed class SignEvent {
    object SignIn : SignEvent()
    object SignUp : SignEvent()
    data class OnEmail(val email: TextFieldValue) : SignEvent()
    data class OnPassword(val password: TextFieldValue) : SignEvent()
}
