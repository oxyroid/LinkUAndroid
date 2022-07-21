package com.linku.im.screen.sign

import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.AuthUseCases
import com.linku.domain.Resource
import com.linku.domain.eventOf
import com.linku.domain.eventOfFailedResource
import com.linku.im.R
import com.linku.im.application
import com.linku.im.vm
import com.linku.im.screen.BaseViewModel
import com.linku.im.global.LinkUEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SignViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : BaseViewModel<SignState, SignEvent>(SignState()) {

    override fun onEvent(event: SignEvent) {
        when (event) {
            is SignEvent.SignIn -> signIn(event.email, event.password)
            is SignEvent.SignUp -> signUp(event.email, event.password)
        }
    }

    private fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = SignState(
                title = application.getString(R.string.information_required)
            )
            return
        }
        authUseCases.signIn(email, password)
            .onEach { resource ->
                _state.value = when (resource) {
                    Resource.Loading -> SignState(
                        loading = true,
                        title = application.getString(R.string.logging)
                    )
                    is Resource.Success -> {
                        vm.onEvent(LinkUEvent.PopBackStack)
                        SignState(
                            loginEvent = eventOf(Unit),
                            title = application.getString(R.string.log_in_success)
                        )
                    }
                    is Resource.Failure -> SignState(
                        error = eventOfFailedResource(resource),
                        title = resource.message
                    )
                }
            }.launchIn(viewModelScope)

    }

    private fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = SignState(
                title = application.getString(R.string.information_required)
            )
            return
        }
        authUseCases.signUp(email, password, email)
            .onEach { resource ->
                _state.value = when (resource) {
                    Resource.Loading -> SignState(
                        loading = true,
                        title = application.getString(R.string.registering)
                    )
                    is Resource.Success -> SignState(
                        registerEvent = eventOf(resource.data),
                        title = application.getString(R.string.register_success)
                    )
                    is Resource.Failure -> SignState(
                        error = eventOfFailedResource(resource),
                        title = resource.message
                    )
                }
            }.launchIn(viewModelScope)
    }
}