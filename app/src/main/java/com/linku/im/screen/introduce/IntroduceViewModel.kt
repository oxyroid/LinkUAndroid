package com.linku.im.screen.introduce

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.DateRange
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material.icons.sharp.Email
import androidx.compose.material.icons.sharp.FormatPaint
import androidx.compose.material.icons.sharp.Language
import androidx.compose.material.icons.sharp.Lock
import androidx.compose.material.icons.sharp.Notifications
import androidx.compose.material.icons.sharp.Upload
import androidx.compose.material.icons.sharp.Visibility
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.ApplicationUseCases
import com.linku.data.usecase.AuthUseCases
import com.linku.data.usecase.Configurations
import com.linku.data.usecase.UserUseCases
import com.linku.domain.Strategy
import com.linku.domain.auth.Authenticator
import com.linku.domain.entity.User
import com.linku.domain.wrapper.Resource
import com.linku.domain.wrapper.eventOf
import com.linku.im.R
import com.linku.im.appyx.target.NavTarget
import com.linku.im.ktx.ifFalse
import com.linku.im.screen.BaseViewModel
import com.linku.im.screen.introduce.composable.Property
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val configurations: Configurations
) : BaseViewModel<IntroduceState, IntroduceEvent>(IntroduceState()) {
    override fun onEvent(event: IntroduceEvent) {
        when (event) {
            is IntroduceEvent.FetchIntroduce -> fetchProfile(event.uid)
            IntroduceEvent.SignOut -> signOut()
            IntroduceEvent.VerifiedEmail -> verifiedEmail()
            is IntroduceEvent.VerifiedEmailCode -> verifiedEmailCode(event.code)
            IntroduceEvent.CancelVerifiedEmail -> cancelVerifiedEmail()
            is IntroduceEvent.Actions -> onActions(event.label, event.actions)
            is IntroduceEvent.Edit -> edit(event)

            IntroduceEvent.ToggleLogMode -> {
                val mode = configurations.isLogMode
                configurations.isLogMode = !mode
                val message = if (configurations.isLogMode) getString(R.string.log_mode_on)
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
                    authUseCases
                        .uploadAvatar(it)
                        .onEach { resource ->
                            writable = when (resource) {
                                Resource.Loading -> readable.copy(
                                    uploading = true
                                )

                                is Resource.Success -> {
                                    readable.copy(
                                        uploading = false
                                    )
                                }

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

    private fun edit(event: IntroduceEvent.Edit) {
        writable = readable.copy(
            editEvent = eventOf(event.type)
        )
    }

    private fun verifiedEmail() {
        viewModelScope.launch {
            writable = readable.copy(
                verifiedEmailStarting = true
            )
            authUseCases.verifiedEmail()
                .onSuccess {
                    writable = readable.copy(
                        verifiedEmailStarting = false,
                        verifiedEmailDialogShowing = true
                    )
                }
                .onFailure {
                    onMessage(it.message)
                    writable = readable.copy(
                        verifiedEmailStarting = false,
                    )
                }
        }
    }

    private fun verifiedEmailCode(code: String) {
        viewModelScope.launch {
            writable = readable.copy(
                verifiedEmailCodeVerifying = true,
                verifiedEmailCodeMessage = ""
            )
            authUseCases.verifiedEmailCode(code)
                .onSuccess {
                    writable = readable.copy(
                        verifiedEmailCodeVerifying = false,
                    )
                    onEvent(IntroduceEvent.FetchIntroduce(readable.uid))
                }
                .onFailure {
                    writable = readable.copy(
                        verifiedEmailCodeVerifying = false,
                        verifiedEmailCodeMessage = it.message ?: ""
                    )
                }

        }
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
            val user = useCases.findUser(currentUid, strategy = Strategy.NetworkElseCache)
            writable = readable.copy(
                uid = currentUid,
                dataProperties = makeDataProperties(user),
                settingsProperties = makeSettingsProperties(),
                avatar = user?.avatar ?: ""
            )
        }
    }

    private val _signOutFlow = MutableSharedFlow<Boolean>()
    val signOutFlow = _signOutFlow.asSharedFlow()

    private fun signOut() {
        viewModelScope.launch {
            authUseCases.signOut()
            _signOutFlow.emit(true)
        }
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


    private fun getString(@StringRes resId: Int): String = applicationUseCases.getString(resId)

    private fun CharSequence?.checkEmpty(): CharSequence =
        if (isNullOrBlank()) getString(R.string.unknown) else this


    private fun makeSettingsProperties(): List<Property> = buildList {
        if (readable.isOthers) return@buildList
        val notification = getString(R.string.profile_settings_notification)
        val safe = getString(R.string.profile_settings_safe)
        val dataSource = getString(R.string.profile_settings_datasource)
        val theme = getString(R.string.profile_settings_theme)
        val language = getString(R.string.profile_settings_language)
        Property.Folder(notification, Icons.Sharp.Notifications, NavTarget.Setting.Notification)
            .addIt()
        Property.Folder(safe, Icons.Sharp.Lock, NavTarget.Setting.Safe).addIt()
        Property.Folder(dataSource, Icons.Sharp.DateRange, NavTarget.Setting.DataSource).addIt()
        Property.Folder(theme, Icons.Sharp.FormatPaint, NavTarget.Setting.Theme).addIt()
        Property.Folder(language, Icons.Sharp.Language, NavTarget.Setting.Language).addIt()
    }

    context(MutableList<E>) private fun <E> E.addIt() = add(this)
}
