package com.linku.im

import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.*
import com.linku.domain.Authenticator
import com.linku.domain.Resource
import com.linku.domain.entity.local.toComposeTheme
import com.linku.domain.repository.SessionRepository
import com.linku.im.network.ConnectivityObserver
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LinkUViewModel @Inject constructor(
    private val messageUseCases: MessageUseCases,
    private val sessionUseCases: SessionUseCases,
    private val emojiUseCases: EmojiUseCases,
    private val applicationUseCases: ApplicationUseCases,
    private val sharedPreference: SharedPreferenceUseCase,
    connectivityObserver: ConnectivityObserver,
    val authenticator: Authenticator,
    private val themes: SettingUseCases.Themes
) : BaseViewModel<LinkUState, LinkUEvent>(LinkUState()) {
    init {
        onEvent(LinkUEvent.InitConfig)
        sessionUseCases.state()
            .onEach { state ->
                when (state) {
                    SessionRepository.State.Default -> deliverState(Label.NoAuth)
                    SessionRepository.State.Connecting -> deliverState(Label.Connecting)
                    SessionRepository.State.Connected -> {}
                    SessionRepository.State.Subscribing -> deliverState(Label.Subscribing)
                    SessionRepository.State.Subscribed -> deliverState(Label.Default)
                    is SessionRepository.State.Failed -> deliverState(Label.Failed)
                    SessionRepository.State.Lost -> deliverState(Label.Failed)
                }
            }
            .launchIn(viewModelScope)
        connectivityObserver.observe()
            .onEach { state ->
                when (state) {
                    ConnectivityObserver.State.Available -> {
                        initRemoteSessionJob?.cancel()
                        initRemoteSessionJob = authenticator.observeCurrent
                            .distinctUntilChanged()
                            .onEach { userId ->
                                if (userId != null) onEvent(LinkUEvent.InitSession)
                                else onEvent(LinkUEvent.Disconnect)
                            }
                            .launchIn(viewModelScope)
                    }
                    ConnectivityObserver.State.Unavailable -> {
                    }
                    ConnectivityObserver.State.Losing -> {
                        initRemoteSessionJob?.cancel()
                    }
                    ConnectivityObserver.State.Lost -> {
                    }
                }
            }
            .launchIn(viewModelScope)

    }

    override fun onEvent(event: LinkUEvent) {
        when (event) {
            LinkUEvent.InitSession -> initRemoteSession()
            LinkUEvent.InitConfig -> initConfig()
            LinkUEvent.ToggleDarkMode -> {
                val saved = !readable.isDarkMode
                sharedPreference.isDarkMode = saved
                writable = readable.copy(
                    isDarkMode = saved
                )
            }
            LinkUEvent.Disconnect -> {
                viewModelScope.launch {
                    sessionUseCases.close()
                }
            }
            is LinkUEvent.OnTheme -> {
                viewModelScope.launch {
                    writable = if (event.isDarkMode) {
                        readable.copy(
                            darkTheme = event.tid.let {
                                themes.findById(it)?.toComposeTheme() ?: readable.darkTheme
                            },
                            isDarkMode = true
                        )
                    } else {
                        readable.copy(
                            lightTheme = event.tid.let {
                                themes.findById(it)?.toComposeTheme() ?: readable.lightTheme
                            },
                            isDarkMode = false
                        )
                    }

                }
            }
        }
    }

    private sealed class Label {
        object Default : Label()
        object Connecting : Label()
        object Failed : Label()
        object Subscribing : Label()
        object SubscribedFailed : Label()
        object NoAuth : Label()
    }

    private fun deliverState(label: Label) {
        writable = readable.copy(
            label = when (label) {
                Label.Default -> {
                    writable = readable.copy(
                        loading = false
                    )
                    null
                }
                Label.Connecting -> {
                    writable = readable.copy(
                        loading = true
                    )
                    applicationUseCases.getString(R.string.connecting)
                }
                Label.Failed -> applicationUseCases.getString(R.string.connected_failed)
                Label.Subscribing -> {
                    writable = readable.copy(
                        loading = true
                    )
                    applicationUseCases.getString(R.string.subscribing)
                }
                Label.SubscribedFailed -> applicationUseCases.getString(R.string.subscribe_failed)
                Label.NoAuth -> applicationUseCases.getString(R.string.no_auth)
            }
        )
    }

    private var initRemoteSessionJob: Job? = null
    private var times = 0
    private fun initRemoteSession() {
        sharedPreference.debug {
            times++
            applicationUseCases.toast("Init session, times: $times")
        }
        initRemoteSessionJob?.cancel()
        initRemoteSessionJob = sessionUseCases
            .init(authenticator.currentUID)
            .onEach { resource ->
                when (resource) {
                    Resource.Loading -> {}
                    is Resource.Success -> {
                        sessionUseCases.subscribeRemote()
                            .onEach {
                                when (it) {
                                    Resource.Loading -> {}
                                    is Resource.Success -> {
                                        messageUseCases.fetchUnreadMessages()
                                        deliverState(Label.Default)
                                        writable = readable.copy(
                                            readyForObserveMessages = true
                                        )
                                    }
                                    is Resource.Failure -> {
                                        deliverState(Label.SubscribedFailed)
                                        writable = readable.copy(
                                            readyForObserveMessages = true
                                        )
                                        delay(3000)
                                        sharedPreference.debug {
                                            applicationUseCases.toast(it.message)
                                        }
                                        onEvent(LinkUEvent.InitSession)
                                    }
                                }
                            }
                            .launchIn(viewModelScope)
                    }
                    is Resource.Failure -> {
                        deliverState(Label.Failed)
                        delay(3000)
                        sharedPreference.debug {
                            applicationUseCases.toast(resource.message)
                        }
                        onEvent(LinkUEvent.InitSession)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun initConfig() {
        fun initEmoji(
            onSuccess: () -> Unit,
            onFailure: (String?) -> Unit
        ) {
            emojiUseCases.initialize()
                .onEach { resource ->
                    when (resource) {
                        Resource.Loading -> {}
                        is Resource.Success -> onSuccess()
                        is Resource.Failure -> onFailure(resource.message)
                    }
                }
                .launchIn(viewModelScope)
        }

        fun initTheme(
            onSuccess: () -> Unit,
            onFailure: (String?) -> Unit
        ) {
            themes.installDefaultTheme()
                .onEach { resource ->
                    when (resource) {
                        Resource.Loading -> {}
                        is Resource.Success -> {
                            writable = readable.copy(
                                lightTheme = themes.findById(
                                    sharedPreference.lightTheme
                                )?.toComposeTheme() ?: readable.lightTheme,
                                darkTheme = themes.findById(
                                    sharedPreference.darkTheme
                                )?.toComposeTheme() ?: readable.darkTheme,
                            )
                            onSuccess()
                        }
                        is Resource.Failure -> onFailure(resource.message)
                    }
                }
                .launchIn(viewModelScope)
        }

        val isDarkMode = sharedPreference.isDarkMode
        initEmoji(
            onSuccess = {
                writable = readable.copy(
                    isDarkMode = isDarkMode,
                    isEmojiReady = true
                )
            },
            onFailure = {
                onMessage(it)
                writable = readable.copy(
                    isDarkMode = isDarkMode,
                    isEmojiReady = true
                )
            }
        )
        initTheme(
            onSuccess = {
                writable = readable.copy(
                    isThemeReady = true
                )
            },
            onFailure = {
                onMessage(it)
                writable = readable.copy(
                    isThemeReady = true
                )
            }
        )
    }

}