package com.linku.im.screen.introduce

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.ApplicationUseCases
import com.linku.data.usecase.AuthUseCases
import com.linku.data.usecase.UserUseCases
import com.linku.domain.Authenticator
import com.linku.domain.Resource
import com.linku.domain.entity.User
import com.linku.domain.eventOf
import com.linku.im.LinkUEvent
import com.linku.im.R
import com.linku.im.screen.BaseViewModel
import com.linku.im.screen.Screen
import com.linku.im.screen.introduce.composable.Property
import com.linku.im.vm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntroduceViewModel @Inject constructor(
    private val useCases: UserUseCases,
    private val authUseCases: AuthUseCases,
    private val applicationUseCases: ApplicationUseCases,
    private val authenticator: Authenticator
) : BaseViewModel<IntroduceState, IntroduceEvent>(IntroduceState()) {

    init {
        _state.value = readable.copy(
            loading = false,
            dataProperties = makeDataProperties(null),
            settingsProperties = makeSettingsProperties()
        )
    }

    override fun onEvent(event: IntroduceEvent) {
        when (event) {
            IntroduceEvent.FetchIntroduce -> fetchProfile()
            IntroduceEvent.SignOut -> signOut()
            IntroduceEvent.VerifiedEmail -> verifiedEmail()
            is IntroduceEvent.VerifiedEmailCode -> verifiedEmailCode(event.code)
            IntroduceEvent.CancelVerifiedEmail -> cancelVerifiedEmail()
            is IntroduceEvent.Actions -> onActions(event.label, event.actions)
            is IntroduceEvent.Edit -> {
                vm.onEvent(
                    LinkUEvent.NavigateWithArgs(
                        Screen.EditScreen.withArgs(event.type)
                    )
                )
                _state.value = readable.copy(
                    editEvent = eventOf(event.type)
                )
            }
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
                        verifiedEmailCodeVerifying = true,
                        verifiedEmailCodeMessage = ""
                    )
                    is Resource.Success -> {
                        _state.value = state.value.copy(
                            verifiedEmailCodeVerifying = false,
                        )
                        onEvent(IntroduceEvent.FetchIntroduce)
                    }
                    is Resource.Failure -> _state.value = state.value.copy(
                        verifiedEmailCodeVerifying = false,
                        verifiedEmailCodeMessage = resource.message
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
        val userId = authenticator.currentUID
        checkNotNull(userId)
        viewModelScope.launch {
            val resource = useCases.findUser(userId)
            _state.value = readable.copy(
                loading = false,
                dataProperties = makeDataProperties(resource),
                settingsProperties = makeSettingsProperties()
            )
        }
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

    private fun makeDataProperties(user: User?): List<Property> = buildList {
        val email = getString(R.string.profile_data_email)
        val name = getString(R.string.profile_data_name)
        val realName = getString(R.string.profile_data_realName)
        val description = getString(R.string.profile_data_description)
        if (user == null) {
            Property.Data(email, null).also(::add)
            Property.Data(name, null).also(::add)
            Property.Data(realName, null).also(::add)
            Property.Data(description, null).also(::add)
        } else {
            val emailActions = buildList {
                if (!user.verified) {
                    Property.Data.Action(
                        text = getString(R.string.email_verified),
                        icon = Icons.Sharp.Email,
                        onClick = {
                            onEvent(IntroduceEvent.VerifiedEmail)
                        }
                    ).also(::add)
                }
            }
            val emailText = if (user.verified) user.email
            else buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        textDecoration = TextDecoration.LineThrough
                    )
                ) {
                    append(user.email)
                }
            }

            Property.Data(email, emailText.checkEmpty(), emailActions).also(::add)

            val nickNameActions = buildList {
                Property.Data.Action(
                    text = getString(R.string.edit),
                    icon = Icons.Sharp.Edit,
                    onClick = {
                        onEvent(IntroduceEvent.Edit(IntroduceEvent.Edit.Type.NickName))
                    }
                ).also(::add)
            }

            Property.Data(name, user.name.checkEmpty(), nickNameActions).also(::add)



            Property.Data(
                key = realName,
                value = if (user.realName == null) applicationUseCases.getString(R.string.profile_data_realName_false)
                else applicationUseCases.getString(R.string.profile_data_realName_true)
            ).also(::add)


            val descriptionActions = buildList {
                Property.Data.Action(
                    text = getString(R.string.edit),
                    icon = Icons.Sharp.Edit,
                    onClick = {
                        onEvent(IntroduceEvent.Edit(IntroduceEvent.Edit.Type.Description))
                    }
                ).also(::add)
            }
            Property.Data(description, "".checkEmpty(), descriptionActions).also(::add)
        }
    }

    private fun makeSettingsProperties(): List<Property> = buildList {
        val notification = getString(R.string.profile_settings_notification)
        val safe = getString(R.string.profile_settings_safe)
        val dataSource = getString(R.string.profile_settings_datasource)
        Property.Folder(notification, Icons.Sharp.Notifications).also(::add)
        Property.Folder(safe, Icons.Sharp.Lock).also(::add)
        Property.Folder(dataSource, Icons.Sharp.DateRange).also(::add)
        Property.Folder(notification, Icons.Sharp.Notifications).also(::add)
        Property.Folder(safe, Icons.Sharp.Lock).also(::add)
        Property.Folder(dataSource, Icons.Sharp.DateRange).also(::add)
    }

    private fun getString(@StringRes resId: Int): String = applicationUseCases.getString(resId)

    private val loadingState get() = state.value.copy(loading = true)

    private fun CharSequence?.checkEmpty(): CharSequence =
        if (isNullOrBlank()) getString(R.string.unknown) else this
}