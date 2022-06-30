package com.linku.im.screen.profile

sealed class ProfileEvent {
    data class FetchProfile(val userId: Int) : ProfileEvent()
    object Logout : ProfileEvent()
}
