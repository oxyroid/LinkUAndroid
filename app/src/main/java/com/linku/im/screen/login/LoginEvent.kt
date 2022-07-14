package com.linku.im.screen.login

sealed class LoginEvent {
    data class SignIn(
        val email: String,
        val password: String
    ) : LoginEvent()

    data class SignUp(
        val email: String,
        val password: String
    ) : LoginEvent()
}
