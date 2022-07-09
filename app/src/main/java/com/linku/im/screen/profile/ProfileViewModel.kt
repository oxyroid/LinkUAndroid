package com.linku.im.screen.profile

import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.AuthUseCases
import com.linku.data.usecase.UserUseCases
import com.linku.domain.Auth
import com.linku.domain.Resource
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val useCases: UserUseCases,
    private val authUseCases: AuthUseCases
) : BaseViewModel<ProfileState, ProfileEvent>(ProfileState()) {
    override fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.FetchProfile -> {
                val userId = Auth.current!!.id
                useCases.findUserUseCase(userId)
                    .onEach { resource ->
                        _state.value = when (resource) {
                            Resource.Loading -> ProfileState(
                                loading = true
                            )

                            is Resource.Success -> state.value.copy(
                                loading = false,
                                email = resource.data.email,
                                emailVerified = resource.data.verified,
                                name = resource.data.name,
                                realName = resource.data.realName ?: "<未知>"
                            )
                            is Resource.Failure -> state.value.copy(
                                loading = false,
                                error = resource.message
                            )
                        }
                    }.launchIn(viewModelScope)
            }
            ProfileEvent.Logout -> {
                authUseCases.logoutUseCase()
                    .onEach { resource ->
                        _state.value = when (resource) {
                            Resource.Loading -> _state.value.copy(loading = true)
                            is Resource.Success -> _state.value.copy(loading = false, logout = true)
                            is Resource.Failure ->
                                _state.value.copy(
                                    loading = false,
                                    error = "退出登录失败"
                                )
                        }
                    }
            }
        }
    }
}