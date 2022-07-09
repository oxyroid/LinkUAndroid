package com.linku.im.screen.login

import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.AuthUseCases
import com.linku.domain.Resource
import com.linku.domain.eventOf
import com.linku.im.R
import com.linku.im.application
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : BaseViewModel<LoginState, LoginEvent>(LoginState()) {

    override fun onEvent(event: LoginEvent) {
        with(event) {
            when (this) {
                is LoginEvent.Login -> {
                    if (email.isBlank() || password.isBlank()) {
                        _state.value = LoginState(
                            title = application.getString(R.string.infomation_required)
                        )
                        return
                    }
                    authUseCases.loginUseCase(email, password)
                        .onEach { resource ->
                            _state.value = when (resource) {
                                Resource.Loading -> LoginState(
                                    loading = true,
                                    title = application.getString(R.string.logging)
                                )
                                is Resource.Success -> {
                                    LoginState(
                                        loginEvent = eventOf(resource.data),
                                        title = application.getString(R.string.log_in_success)
                                    )
                                }
                                is Resource.Failure -> LoginState(
                                    error = eventOf(resource.message),
                                    title = resource.message
                                )
                            }
                        }.launchIn(viewModelScope)
                }
                is LoginEvent.Register -> {
                    if (email.isBlank() || password.isBlank()) {
                        _state.value = LoginState(
                            title = application.getString(R.string.infomation_required)
                        )
                        return
                    }
                    authUseCases.registerUseCase(email, password, email)
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
                                    error = eventOf(resource.message),
                                    title = resource.message
                                )
                            }
                        }.launchIn(viewModelScope)
                }
            }

        }
    }
}