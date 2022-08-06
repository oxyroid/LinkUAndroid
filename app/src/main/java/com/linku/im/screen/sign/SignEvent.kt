package com.linku.im.screen.sign

sealed class SignEvent {
    object SignIn : SignEvent()
    object SignUp : SignEvent()
    data class OnEmail(val email: String) : SignEvent()
    data class OnPassword(val password: String) : SignEvent()
}
