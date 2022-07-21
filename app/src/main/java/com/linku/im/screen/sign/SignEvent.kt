package com.linku.im.screen.sign

sealed class SignEvent {
    data class SignIn(
        val email: String,
        val password: String
    ) : SignEvent()

    data class SignUp(
        val email: String,
        val password: String
    ) : SignEvent()
}
