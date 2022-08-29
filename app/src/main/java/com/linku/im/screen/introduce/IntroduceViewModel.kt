package com.linku.im.screen.introduce

import android.net.Uri
import androidx.activity.compose.LocalActivityResultRegistryOwner
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
import com.linku.data.usecase.SettingUseCase
import com.linku.data.usecase.UserUseCases
import com.linku.domain.Authenticator
import com.linku.domain.Resource
import com.linku.domain.entity.User
import com.linku.domain.eventOf
import com.linku.im.R
import com.linku.im.extension.ifFalse
import com.linku.im.screen.BaseViewModel
import com.linku.im.screen.introduce.composable.Property
import com.yalantis.ucrop.UCrop
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
    private val authenticator: Authenticator,
    private val settings: SettingUseCase
) : BaseViewModel<IntroduceState, IntroduceEvent>(IntroduceState()) {

    override fun onEvent(event: IntroduceEvent) {
        when (event) {
            is IntroduceEvent.FetchIntroduce -> fetchProfile(event.uid)
            IntroduceEvent.SignOut -> signOut()
            IntroduceEvent.VerifiedEmail -> verifiedEmail()
            is IntroduceEvent.VerifiedEmailCode -> verifiedEmailCode(event.code)
            IntroduceEvent.CancelVerifiedEmail -> cancelVerifiedEmail()
            is IntroduceEvent.Actions -> onActions(event.label, event.actions)
            is IntroduceEvent.Edit -> {
                writable = readable.copy(
                    editEvent = eventOf(event.type)
                )
            }
            IntroduceEvent.ToggleLogMode -> {
                val mode = settings.isLogMode
                settings.isLogMode = !mode
                val message = if (settings.isLogMode) getString(R.string.log_mode_on)
                else getString(R.string.log_mode_off)
                onMessage(message)
            }
            IntroduceEvent.AvatarClicked -> {
                onActions(getString(R.string.profile_avatar_label), buildList {
                    Property.Data.Action(
                        text = getString(R.string.profile_avatar_visit),
                        icon = Icons.Sharp.Visibility,
                        onClick = {
                            writable = readable.copy(
                                preview = readable.avatar
                            )
                        }
                    ).also(::add)
                    readable.isOthers.ifFalse {
                        Property.Data.Action(
                            text = getString(R.string.profile_avatar_update),
                            icon = Icons.Sharp.Upload,
                            onClick = {
                                writable = readable.copy(
                                    runLauncher = eventOf(Unit)
                                )
                            }
                        ).also(::add)
                    }
                })
            }
            IntroduceEvent.DismissPreview -> writable = readable.copy(
                preview = ""
            )
            is IntroduceEvent.UpdateAvatar -> {
                val uri = event.uri
                writable = readable.copy(
                    avatar = uri?.toString() ?: "",
                )
                uri?.also {
                    authUseCases.uploadAvatar(it)
                        .onEach { resource ->
                            writable = when (resource) {
                                Resource.Loading -> readable.copy(
                                    uploading = true
                                )
                                is Resource.Success -> readable.copy(
                                    uploading = false
                                )
                                is Resource.Failure -> {
                                    onMessage(resource.message)
                                    readable.copy(
                                        uploading = false,
                                        avatar = ""
                                    )
                                }
                            }
                        }
                        .launchIn(viewModelScope)
                }
            }
        }
    }

    private fun verifiedEmail() {
        authUseCases.verifiedEmail()
            .onEach { resource ->
                writable = when (resource) {
                    Resource.Loading -> readable.copy(
                        verifiedEmailStarting = true
                    )
                    is Resource.Success -> readable.copy(
                        verifiedEmailStarting = false,
                        verifiedEmailDialogShowing = true
                    )
                    is Resource.Failure -> {
                        onMessage(resource.message)
                        readable.copy(
                            verifiedEmailStarting = false,
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun verifiedEmailCode(code: String) {
        authUseCases.verifiedEmailCode(code)
            .onEach { resource ->
                when (resource) {
                    Resource.Loading -> writable = readable.copy(
                        verifiedEmailCodeVerifying = true,
                        verifiedEmailCodeMessage = ""
                    )
                    is Resource.Success -> {
                        writable = readable.copy(
                            verifiedEmailCodeVerifying = false,
                        )
                        onEvent(IntroduceEvent.FetchIntroduce(readable.uid))
                    }
                    is Resource.Failure -> writable = readable.copy(
                        verifiedEmailCodeVerifying = false,
                        verifiedEmailCodeMessage = resource.message
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun cancelVerifiedEmail() {
        writable = readable.copy(
            verifiedEmailDialogShowing = false
        )
    }

    private fun fetchProfile(uid: Int) {
        val currentUid = if (uid == -1) authenticator.currentUID ?: uid else uid
        writable = readable.copy(
            uploading = false,
            isOthers = currentUid != authenticator.currentUID,
            dataProperties = makeDataProperties(null)
        )
        viewModelScope.launch {
            val user = useCases.findUser(currentUid)
            writable = readable.copy(
                uid = currentUid,
                dataProperties = makeDataProperties(user),
                settingsProperties = makeSettingsProperties(),
                avatar = user?.avatar ?: ""
            )
        }
    }

    private fun signOut() {
        authUseCases.signOut()
            .onEach { resource ->
                writable = when (resource) {
                    Resource.Loading -> readable.copy(uploading = true)
                    is Resource.Success -> writable.copy(uploading = false, logout = true)
                    is Resource.Failure -> {
                        onMessage(getString(R.string.sign_out_failed))
                        writable.copy(
                            uploading = false,
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun onActions(label: String, actions: List<Property.Data.Action>) {
        writable = readable.copy(
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
            readable.isOthers.ifFalse {
                Property.Data(realName, null).also(::add)
            }
            Property.Data(description, null).also(::add)
        } else {
            val emailActions = buildList {
                if (!readable.isOthers && !user.verified) {
                    Property.Data.Action(
                        text = getString(R.string.email_verified),
                        icon = Icons.Sharp.Email,
                        onClick = {
                            onEvent(IntroduceEvent.VerifiedEmail)
                        }
                    ).also(::add)
                }
            }
            val emailText = if (readable.isOthers || user.verified) user.email
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
                readable.isOthers.ifFalse {
                    Property.Data.Action(
                        text = getString(R.string.edit),
                        icon = Icons.Sharp.Edit,
                        onClick = {
                            onEvent(IntroduceEvent.Edit(IntroduceEvent.Edit.Type.NickName))
                        }
                    ).also(::add)
                }
            }

            Property.Data(name, user.name.checkEmpty(), nickNameActions).also(::add)


            readable.isOthers.ifFalse {
                Property.Data(
                    key = realName,
                    value = if (user.realName == null) applicationUseCases.getString(R.string.profile_data_realName_false)
                    else applicationUseCases.getString(R.string.profile_data_realName_true)
                ).also(::add)
            }

            val descriptionActions = buildList {

                readable.isOthers.ifFalse {
                    Property.Data.Action(
                        text = getString(R.string.edit),
                        icon = Icons.Sharp.Edit,
                        onClick = {
                            onEvent(IntroduceEvent.Edit(IntroduceEvent.Edit.Type.Description))
                        }
                    ).also(::add)
                }
            }
            Property.Data(description, "".checkEmpty(), descriptionActions).also(::add)
        }
    }

    private fun makeSettingsProperties(): List<Property> = buildList {
        if (readable.isOthers) return@buildList
        val notification = getString(R.string.profile_settings_notification)
        val safe = getString(R.string.profile_settings_safe)
        val dataSource = getString(R.string.profile_settings_datasource)
        val theme = getString(R.string.profile_settings_theme)
        val language = getString(R.string.profile_settings_language)
        Property.Folder(notification, Icons.Sharp.Notifications).also(::add)
        Property.Folder(safe, Icons.Sharp.Lock).also(::add)
        Property.Folder(dataSource, Icons.Sharp.DateRange).also(::add)
        Property.Folder(theme, Icons.Sharp.FormatPaint).also(::add)
        Property.Folder(language, Icons.Sharp.Language).also(::add)
    }

    private fun getString(@StringRes resId: Int): String = applicationUseCases.getString(resId)

    private fun CharSequence?.checkEmpty(): CharSequence =
        if (isNullOrBlank()) getString(R.string.unknown) else this
}