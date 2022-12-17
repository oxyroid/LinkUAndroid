package com.linku.im.screen.introduce

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.viewModelScope
import com.linku.core.wrapper.Resource
import com.linku.core.wrapper.eventOf
import com.linku.data.Configurations
import com.linku.data.usecase.*
import com.linku.domain.Strategy
import com.linku.domain.auth.Authenticator
import com.linku.domain.entity.ContactRequest
import com.linku.domain.entity.Message
import com.linku.domain.entity.User
import com.linku.im.R
import com.linku.im.appyx.target.NavTarget
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
    private val applications: ApplicationUseCases,
    private val authenticator: Authenticator,
    private val configurations: Configurations,
    private val messages: MessageUseCases,
    private val conversations: ConversationUseCases
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
            IntroduceEvent.ToggleLogMode -> toggleLogMode()
            IntroduceEvent.AvatarClicked -> avatarClicked()
            is IntroduceEvent.UpdateAvatar -> updateAvatar(event)
            IntroduceEvent.FriendShipAction -> friendshipAction()
        }
    }

    private fun friendshipAction() {
        when (val category = readable.category) {
            Category.Personal -> {}
            is Category.User -> {
                when (val friendship = category.friendship) {
                    Friendship.Loading -> {}
                    Friendship.None -> {
                        // TODO send a friendship request
                        messages.contactRequest(
                            uid = readable.uid
                        ).onEach { resource ->
                            writable = when (resource) {
                                Resource.Loading -> readable.copy(
                                    category = category.copy(
                                        friendship = Friendship.Loading
                                    )
                                )

                                is Resource.Success -> readable.copy(
                                    category = category.copy(
                                        friendship = Friendship.Pending(false)
                                    )
                                )

                                is Resource.Failure -> {
                                    onMessage(resource.message)
                                    readable.copy(
                                        category = category.copy(
                                            friendship = Friendship.None
                                        )
                                    )
                                }
                            }
                        }.launchIn(viewModelScope)
                    }

                    is Friendship.Pending -> {}
                    is Friendship.Completed -> {
                        // TODO make a chat
                        writable = readable.copy(
                            goChat = eventOf(friendship.cid)
                        )
                    }
                }
            }
        }
    }

    private fun updateAvatar(event: IntroduceEvent.UpdateAvatar) {
        val uri = event.uri
        writable = readable.copy(
            avatar = uri?.toString() ?: "",
        )
        uri?.also {
            authUseCases.uploadAvatar(it).onEach { resource ->
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
                            uploading = false, avatar = ""
                        )
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun avatarClicked() {
        onActions(getString(R.string.profile_avatar_label), buildList {
            when (readable.category) {
                Category.Personal -> {
                    Property.Data.Action(text = getString(R.string.profile_avatar_update),
                        icon = Icons.Rounded.Upload,
                        onClick = {
                            writable = readable.copy(
                                runLauncher = eventOf(Unit)
                            )
                        })
                }
                is Category.User -> {}
            }
        })
    }

    private fun toggleLogMode() {
        val mode = configurations.isLogMode
        configurations.isLogMode = !mode
        val message = if (configurations.isLogMode) getString(R.string.log_mode_on)
        else getString(R.string.log_mode_off)
        onMessage(message)
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
            authUseCases.verifiedEmail().onSuccess {
                writable = readable.copy(
                    verifiedEmailStarting = false, verifiedEmailDialogShowing = true
                )
            }.onFailure {
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
                verifiedEmailCodeVerifying = true, verifiedEmailCodeMessage = ""
            )
            authUseCases.verifiedEmailCode(code).onSuccess {
                writable = readable.copy(
                    verifiedEmailCodeVerifying = false,
                )
                onEvent(IntroduceEvent.FetchIntroduce(readable.uid))
            }.onFailure {
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
        viewModelScope.launch {
            val currentUID = if (uid == -1) authenticator.currentUID ?: uid else uid
            val isOther = currentUID != authenticator.currentUID

            val user = useCases.findUser(currentUID, strategy = Strategy.NetworkElseCache)
            if (isOther) {
                writable = readable.copy(
                    category = Category.User()
                )
                val cid = conversations.findChatRoom(uid)
                if (cid != null) {
                    writable = readable.copy(
                        uid = currentUID,
                        category = Category.User(
                            friendship = Friendship.Completed(cid)
                        ),
                        dataProperties = makeDataProperties(user),
                        settingsProperties = makeSettingsProperties(),
                        avatar = user?.avatar ?: ""
                    )
                } else {
                    val requests =
                        messages.findMessagesByType<ContactRequest>(Message.Type.ContactRequest)
                    var friendship: Friendship =
                        if (requests.isEmpty()) Friendship.None else Friendship.Loading
                    for (request in requests) {
                        val requestFrom = request.uid
                        val requestTo = request.tid
                        if (requestFrom == currentUID && requestTo == uid) {
                            friendship = Friendship.Pending(false)
                            break
                        } else if (requestFrom == uid && requestTo == currentUID) {
                            friendship = Friendship.Pending(true)
                            break
                        } else {
                            friendship = Friendship.None
                            break
                        }
                    }
                    val category = Category.User(
                        friendship = friendship
                    )
                    writable = readable.copy(
                        uid = currentUID,
                        category = category,
                        dataProperties = makeDataProperties(user),
                        settingsProperties = makeSettingsProperties(),
                        avatar = user?.avatar ?: ""
                    )
                }


            } else {
                writable = readable.copy(
                    uploading = false,
                    category = Category.Personal,
                    dataProperties = makeDataProperties(null)
                )
                writable = readable.copy(
                    uid = currentUID,
                    dataProperties = makeDataProperties(user),
                    settingsProperties = makeSettingsProperties(),
                    avatar = user?.avatar ?: ""
                )
            }
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
            actions = actions, actionsLabel = label
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
            if (readable.category == Category.Personal) {
                Property.Data(realName, null).also(::add)
            }
            Property.Data(description, null).also(::add)
        } else {
            val emailActions = buildList {
                if (readable.category == Category.Personal) {
                    if (!user.verified) {
                        Property.Data.Action(text = getString(R.string.email_verified),
                            icon = Icons.Rounded.Email,
                            onClick = {
                                onEvent(IntroduceEvent.VerifiedEmail)
                            }).also(::add)
                    } else {
                        Property.Data.Action(text = getString(R.string.email_verified_already),
                            icon = Icons.Rounded.Email,
                            onClick = {

                            }).also(::add)
                    }
                }
            }
            val emailText = if (readable.category is Category.User || user.verified) user.email
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
                if (readable.category == Category.Personal) {
                    Property.Data.Action(text = getString(R.string.edit),
                        icon = Icons.Rounded.Edit,
                        onClick = {
                            onEvent(IntroduceEvent.Edit(IntroduceEvent.Edit.Type.NickName))
                        }).also(::add)
                }
            }

            Property.Data(name, user.name.checkEmpty(), nickNameActions).also(::add)


            if (readable.category == Category.Personal) {
                Property.Data(
                    key = realName,
                    value = if (user.realName == null) applications.getString(R.string.profile_data_realName_false)
                    else applications.getString(R.string.profile_data_realName_true)
                ).also(::add)
            }

            val descriptionActions = buildList {
                if (readable.category == Category.Personal) {
                    Property.Data.Action(text = getString(R.string.edit),
                        icon = Icons.Rounded.Edit,
                        onClick = {
                            onEvent(IntroduceEvent.Edit(IntroduceEvent.Edit.Type.Description))
                        }).also(::add)
                }
            }

            Property.Data(description, "".checkEmpty(), descriptionActions).also(::add)
        }
    }


    private fun getString(@StringRes resId: Int): String = applications.getString(resId)

    private fun CharSequence?.checkEmpty(): CharSequence = if (isNullOrBlank()) "--" else this

    private fun makeSettingsProperties(): List<Property> = run {
        if (readable.category is Category.User) return@run emptyList()
        val notification = getString(R.string.profile_settings_notification)
        val safe = getString(R.string.profile_settings_safe)
        val dataSource = getString(R.string.profile_settings_datasource)
        val theme = getString(R.string.profile_settings_theme)
        val language = getString(R.string.profile_settings_language)
        listOf(
            Property.Folder(
                notification, Icons.Rounded.Notifications, NavTarget.Setting.Notification
            ),
            Property.Folder(safe, Icons.Rounded.Lock, NavTarget.Setting.Safe),
            Property.Folder(dataSource, Icons.Rounded.DateRange, NavTarget.Setting.DataSource),
            Property.Folder(theme, Icons.Rounded.FormatPaint, NavTarget.Setting.Theme),
            Property.Folder(language, Icons.Rounded.Language, NavTarget.Setting.Language)
        )
    }

}
