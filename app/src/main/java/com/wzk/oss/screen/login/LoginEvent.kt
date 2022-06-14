package com.wzk.oss.screen.login

sealed class LoginEvent {
    data class Login(
        val email: String,
        val password: String
    ) : LoginEvent()

    data class Register(
        val email: String,
        val password: String,
        val username: String
    ) : LoginEvent()
}
