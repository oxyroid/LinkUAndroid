package com.linku.im.screen.profile

import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(

) : BaseViewModel<ProfileState, ProfileEvent>(ProfileState()) {
    override fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.FetchProfile -> {

            }
            ProfileEvent.Logout -> TODO()
        }
    }
}