package com.linku.im.screen.introduce

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.AuthUseCases
import com.linku.data.usecase.UserUseCases
import com.linku.domain.Auth
import com.linku.domain.Resource
import com.linku.domain.entity.UserDTO
import com.linku.domain.eventOf
import com.linku.im.R
import com.linku.im.application
import com.linku.im.screen.BaseViewModel
import com.linku.im.screen.introduce.composable.Property
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class IntroduceViewModel @Inject constructor(
    private val useCases: UserUseCases,
    private val authUseCases: AuthUseCases
) : BaseViewModel<IntroduceState, IntroduceEvent>(IntroduceState()) {
    override fun onEvent(event: IntroduceEvent) {
        when (event) {
            IntroduceEvent.FetchIntroduce -> fetchProfile()
            IntroduceEvent.SignOut -> signOut()
            IntroduceEvent.VerifiedEmail -> verifiedEmail()
            is IntroduceEvent.VerifiedEmailCode -> verifiedEmailCode(event.code)
            IntroduceEvent.CancelVerifiedEmail -> cancelVerifiedEmail()
            is IntroduceEvent.Actions -> onActions(event.label, event.actions)
        }
    }

    private fun verifiedEmail() {
        authUseCases.verifiedEmail()
            .onEach { resource ->
                _state.value = when (resource) {
                    Resource.Loading -> state.value.copy(
                        verifiedEmailStarting = true
                    )
                    is Resource.Success -> state.value.copy(
                        verifiedEmailStarting = false,
                        verifiedEmailDialogShowing = true
                    )
                    is Resource.Failure -> state.value.copy(
                        verifiedEmailStarting = false,
                        error = eventOf(resource.message)
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun verifiedEmailCode(code: String) {
        authUseCases.verifiedEmailCode(code)
            .onEach { resource ->
                when (resource) {
                    Resource.Loading -> _state.value = state.value.copy(
                        verifiedEmailCodeVerifying = true
                    )
                    is Resource.Success -> {
                        _state.value = state.value.copy(
                            verifiedEmailCodeVerifying = false,
                        )
                        onEvent(IntroduceEvent.FetchIntroduce)
                    }
                    is Resource.Failure -> _state.value = state.value.copy(
                        verifiedEmailCodeVerifying = false,
                        error = eventOf(resource.message)
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun cancelVerifiedEmail() {
        _state.value = state.value.copy(
            verifiedEmailDialogShowing = false
        )
    }

    private fun fetchProfile() {
        val userId = Auth.currentUID
        checkNotNull(userId)
        useCases.findUser(userId)
            .onEach { resource ->
                _state.value = when (resource) {
                    Resource.Loading -> loadingState
                    is Resource.Success -> state.value.copy(
                        loading = false,
                        dataProperties = makeDataProperties(resource.data),
                        settingsProperties = makeSettingsProperties()
                    )
                    is Resource.Failure -> state.value.copy(
                        loading = false,
                        error = eventOf(resource.message)
                    )
                }
            }.launchIn(viewModelScope)
    }

    private fun signOut() {
        authUseCases.logout()
            .onEach { resource ->
                _state.value = when (resource) {
                    Resource.Loading -> loadingState
                    is Resource.Success -> _state.value.copy(loading = false, logout = true)
                    is Resource.Failure -> _state.value.copy(
                        loading = false,
                        error = eventOf(getString(R.string.sign_out_failed))
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun onActions(label: String, actions: List<Property.Data.Action>) {
        _state.value = state.value.copy(
            actions = actions,
            actionsLabel = label
        )
    }

    private fun makeDataProperties(userDTO: UserDTO?): List<Property> = buildList {
        val email = getString(R.string.profile_data_email)
        val name = getString(R.string.profile_data_name)
        val realName = getString(R.string.profile_data_realName)
        val description = getString(R.string.profile_data_description)
        if (userDTO == null) {
            Property.Data(email, null).also(::add)
            Property.Data(name, null).also(::add)
            Property.Data(realName, null).also(::add)
            Property.Data(description, null).also(::add)
        } else {
            val emailActions = buildList {
                if (!userDTO.verified) {
                    Property.Data.Action(
                        text = getString(R.string.email_verified),
                        icon = Icons.Rounded.Email,
                        onClick = {
                            onEvent(IntroduceEvent.VerifiedEmail)
                        }
                    ).also(::add)
                }
            }
            val emailText = if (userDTO.verified) userDTO.email
            else buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        textDecoration = TextDecoration.LineThrough
                    )
                ) {
                    append(userDTO.email)
                }
            }

            Property.Data(email, emailText.checkEmpty(), emailActions).also(::add)
            Property.Data(name, userDTO.name.checkEmpty()).also(::add)
            Property.Data(
                realName,
                if (userDTO.realName == null) application.getString(R.string.profile_data_realName_false)
                else application.getString(R.string.profile_data_realName_true)
            ).also(::add)
            // FIXME
            Property.Data(description, userDTO.salt.checkEmpty()).also(::add)
        }
    }

    private fun makeSettingsProperties(): List<Property> = buildList {
        val notification = getString(R.string.profile_settings_notification)
        val safe = getString(R.string.profile_settings_safe)
        val dataSource = getString(R.string.profile_settings_datasource)
        Property.Folder(notification, Icons.Rounded.Notifications).also(::add)
        Property.Folder(safe, Icons.Rounded.Lock).also(::add)
        Property.Folder(dataSource, Icons.Rounded.DateRange).also(::add)
    }

    private fun getString(@StringRes resId: Int): String = application.getString(resId)

    private val loadingState get() = state.value.copy(loading = true)

    private fun CharSequence?.checkEmpty(): CharSequence =
        if (isNullOrBlank()) getString(R.string.unknown) else this
}