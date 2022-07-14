package com.linku.im.screen.login

import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.AuthUseCases
import com.linku.domain.Auth
import com.linku.domain.Resource
import com.linku.domain.eventOf
import com.linku.domain.eventOfFailedResource
import com.linku.im.R
import com.linku.im.application
import com.linku.im.overall
import com.linku.im.screen.BaseViewModel
import com.linku.im.screen.overall.OverallEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : BaseViewModel<LoginState, LoginEvent>(LoginState()) {

    override fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.SignIn -> signIn(event.email, event.password)
            is LoginEvent.SignUp -> signUp(event.email, event.password)
        }
    }

    private fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = LoginState(
                title = application.getString(R.string.information_required)
            )
            return
        }
        authUseCases.signInUseCase(email, password)
            .onEach { resource ->
                _state.value = when (resource) {
                    Resource.Loading -> LoginState(
                        loading = true,
                        title = application.getString(R.string.logging)
                    )
                    is Resource.Success -> {
                        overall.onEvent(OverallEvent.PopBackStack)
                        LoginState(
                            loginEvent = eventOf(Unit),
                            title = application.getString(R.string.log_in_success)
                        )
                    }
                    is Resource.Failure -> LoginState(
                        error = eventOfFailedResource(resource),
                        title = resource.message
                    )
                }
            }.launchIn(viewModelScope)

    }

    private fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = LoginState(
                title = application.getString(R.string.information_required)
            )
            return
        }
        authUseCases.signUpUseCase(email, password, email)
            .onEach { resource ->
                _state.value = when (resource) {
                    Resource.Loading -> LoginState(
                        loading = true,
                        title = application.getString(R.string.registering)
                    )
                    is Resource.Success -> LoginState(
                        registerEvent = eventOf(resource.data),
                        title = application.getString(R.string.register_success)
                    )
                    is Resource.Failure -> LoginState(
                        error = eventOfFailedResource(resource),
                        title = resource.message
                    )
                }
            }.launchIn(viewModelScope)
    }
}