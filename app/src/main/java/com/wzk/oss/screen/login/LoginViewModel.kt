package com.wzk.oss.screen.login

import androidx.lifecycle.viewModelScope
import com.wzk.domain.usecase.UserUseCases
import com.wzk.wrapper.Resource
import com.wzk.wrapper.eventOf
import com.wzk.oss.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userUseCases: UserUseCases
) : BaseViewModel<LoginState, LoginEvent>(LoginState()) {
    override fun onEvent(event: LoginEvent) {
        with(event) {
            when (this) {
                is LoginEvent.Login -> {
                    userUseCases.loginUseCase(email, password)
                        .onEach { resource ->
                            _state.value = when (resource) {
                                Resource.Loading -> LoginState(loading = true)
                                is Resource.Success -> LoginState(loginEvent = eventOf(resource.data))
                                is Resource.Failure -> LoginState(error = eventOf(resource.message))
                            }
                        }.launchIn(viewModelScope)
                }
                is LoginEvent.Register -> {
                    userUseCases.registerUseCase(email, password, username)
                        .onEach { resource ->
                            _state.value = when (resource) {
                                Resource.Loading -> LoginState(loading = true)
                                is Resource.Success -> LoginState(registerEvent = eventOf(resource.data))
                                is Resource.Failure -> LoginState(error = eventOf(resource.message))
                            }
                        }.launchIn(viewModelScope)
                }
            }

        }
    }
}