package com.linku.im

import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.*
import com.linku.domain.Authenticator
import com.linku.domain.Resource
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
    private val settings: SettingUseCase,
    connectivityObserver: ConnectivityObserver,
    val authenticator: Authenticator
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
                        sessionJob?.cancel()
                        sessionJob = authenticator.observeCurrent
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
                        sessionJob?.cancel()
                    }
                    ConnectivityObserver.State.Lost -> {
                    }
                }
            }
            .launchIn(viewModelScope)

    }

    override fun onEvent(event: LinkUEvent) {
        when (event) {
            LinkUEvent.InitSession -> initSession()
            LinkUEvent.InitConfig -> initConfig()
            LinkUEvent.ToggleDarkMode -> {
                val saved = !readable.isDarkMode
                settings.isDarkMode = saved
                writable = readable.copy(
                    isDarkMode = saved
                )
            }
            LinkUEvent.Disconnect -> {
                viewModelScope.launch {
                    sessionUseCases.close()
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

    private var sessionJob: Job? = null
    private var times = 0
    private fun initSession() {
        settings.debug {
            times++
            applicationUseCases.toast("Init session, times: $times")
        }
        sessionJob?.cancel()
        sessionJob = sessionUseCases
            .init(authenticator.currentUID)
            .onEach { resource ->
                when (resource) {
                    Resource.Loading -> {}
                    is Resource.Success -> {
                        sessionUseCases.subscribe()
                            .onEach { subscribeResource ->
                                when (subscribeResource) {
                                    Resource.Loading -> {}
                                    is Resource.Success -> {
                                        messageUseCases.fetchUnreadMessages()
                                        deliverState(Label.Default)
                                        writable = readable.copy(
                                            hasSynced = true
                                        )
                                    }
                                    is Resource.Failure -> {
                                        deliverState(Label.SubscribedFailed)
                                        delay(3000)
                                        settings.debug {
                                            applicationUseCases.toast(subscribeResource.message)
                                        }
                                        onEvent(LinkUEvent.InitSession)
                                        writable = readable.copy(
                                            hasSynced = true
                                        )
                                    }
                                }
                            }
                            .launchIn(viewModelScope)
                    }
                    is Resource.Failure -> {
                        deliverState(Label.Failed)
                        delay(3000)
                        settings.debug {
                            applicationUseCases.toast(resource.message)
                        }
                        onEvent(LinkUEvent.InitSession)
                    }
                }
            }
            .launchIn(viewModelScope)
    }


    private fun initConfig() {
        val isDarkMode = settings.isDarkMode
        emojiUseCases.initialize()
            .onEach { resource ->
                when (resource) {
                    Resource.Loading -> {}
                    is Resource.Success -> {
                        writable = readable.copy(
                            isDarkMode = isDarkMode,
                            isEmojiReady = true
                        )
                    }
                    is Resource.Failure -> {
                        writable = readable.copy(
                            isDarkMode = isDarkMode,
                            isEmojiReady = true,
                        )
                        onMessage(resource.message)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

}