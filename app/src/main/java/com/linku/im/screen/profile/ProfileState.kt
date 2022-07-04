package com.linku.im.screen.profile

data class ProfileState(
    val email: String? = null,
    val emailVerified: Boolean = false,
    val name: String? = null,
    val realName: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val logout: Boolean = false
)
