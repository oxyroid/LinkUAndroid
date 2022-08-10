package com.linku.im.screen.sign

import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.ApplicationUseCases
import com.linku.data.usecase.AuthUseCases
import com.linku.domain.Resource
import com.linku.domain.eventOf
import com.linku.im.LinkUEvent
import com.linku.im.R
import com.linku.im.screen.BaseViewModel
import com.linku.im.vm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SignViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val applicationUseCases: ApplicationUseCases
) : BaseViewModel<SignState, SignEvent>(SignState()) {

    override fun onEvent(event: SignEvent) {
        when (event) {
            SignEvent.SignIn -> signIn()
            SignEvent.SignUp -> signUp()
            is SignEvent.OnEmail -> _state.value = readable.copy(
                email = event.email
            )
            is SignEvent.OnPassword -> _state.value = readable.copy(
                password = event.password
            )
        }
    }

    private fun signIn() {
        val email = readable.email
        val password = readable.password
        if (email.isBlank() || password.isBlank()) {
            _state.value = readable.copy(
                error = eventOf(applicationUseCases.getString(R.string.information_required)),
                loading = false
            )
            return
        }
        authUseCases.signIn(email, password)
            .onEach { resource ->
                _state.value = when (resource) {
                    Resource.Loading -> readable.copy(
                        loading = true,
                    )
                    is Resource.Success -> {
                        vm.onEvent(LinkUEvent.PopBackStack)
                        readable.copy(
                            loginEvent = eventOf(Unit),
                            error = eventOf(applicationUseCases.getString(R.string.log_in_success)),
                            loading = false
                        )
                    }
                    is Resource.Failure -> readable.copy(
                        error = eventOf(resource.message),
                        loading = false
                    )
                }
            }
            .launchIn(viewModelScope)

    }

    private fun signUp() {
        val email = readable.email
        val password = readable.password
        if (email.isBlank() || password.isBlank()) {
            _state.value = readable.copy(
                error = eventOf(applicationUseCases.getString(R.string.information_required)),
                loading = false
            )
            return
        }
        authUseCases.signUp(email, password, email)
            .onEach { resource ->
                _state.value = when (resource) {
                    Resource.Loading -> readable.copy(
                        loading = true,
                    )
                    is Resource.Success -> readable.copy(
                        registerEvent = eventOf(resource.data),
                        error = eventOf(applicationUseCases.getString(R.string.register_success)),
                        loading = false
                    )
                    is Resource.Failure -> readable.copy(
                        error = eventOf(resource.message),
                        loading = false
                    )
                }
            }.launchIn(viewModelScope)
    }
}