package com.linku.im.screen.sign

import androidx.lifecycle.viewModelScope
import com.linku.core.wrapper.Resource
import com.linku.core.wrapper.eventOf
import com.linku.data.usecase.ApplicationUseCases
import com.linku.data.usecase.AuthUseCases
import com.linku.domain.service.system.SensorService
import com.linku.im.R
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val applicationUseCases: ApplicationUseCases,
    sensorService: SensorService
) : BaseViewModel<SignState, SignEvent>(SignState()) {
    override fun onEvent(event: SignEvent) {
        when (event) {
            SignEvent.SignIn -> signIn()
            SignEvent.SignUp -> signUp()
            is SignEvent.OnEmail -> writable = readable.copy(
                email = event.email
            )

            is SignEvent.OnPassword -> writable = readable.copy(
                password = event.password
            )
        }
    }

    private val _point3DFlow = MutableSharedFlow<Float>()
    val point3DFlow: SharedFlow<Float> get() = _point3DFlow

    init {
        sensorService.observe()
            .map { it.x }
            .onEach {
                _point3DFlow.emit(it)
            }
            .launchIn(viewModelScope)
    }

    private fun signIn() {
        val email = readable.email.text
        val password = readable.password.text
        if (email.isBlank() || password.isBlank()) {
            onMessage(applicationUseCases.getString(R.string.information_required))
            writable = readable.copy(
                loading = false
            )
            return
        }
        authUseCases.signIn(email, password)
            .onEach { resource ->
                writable = when (resource) {
                    Resource.Loading -> {
                        readable.copy(
                            loading = true
                        )
                    }

                    is Resource.Success -> {
                        readable.copy(
                            loading = false,
                            loginEvent = eventOf(Unit)
                        )
                    }

                    is Resource.Failure -> {
                        onMessage(resource.message)
                        readable.copy(
                            loading = false
                        )
                    }
                }
            }
            .launchIn(viewModelScope)

    }

    private fun signUp() {
        val email = readable.email.text
        val password = readable.password.text
        if (email.isBlank() || password.isBlank()) {
            onMessage(applicationUseCases.getString(R.string.information_required))
            writable = readable.copy(
                loading = false
            )
            return
        }
        viewModelScope.launch {
            writable = readable.copy(
                loading = true
            )
            authUseCases.signUp(email, password, email)
                .onSuccess {
                    onMessage(applicationUseCases.getString(R.string.register_success))
                    writable = readable.copy(
                        loading = false
                    )
                }
                .onFailure {
                    onMessage(it.message)
                    writable = readable.copy(
                        loading = false
                    )
                }

        }
    }
}
