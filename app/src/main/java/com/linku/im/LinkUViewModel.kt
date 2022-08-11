package com.linku.im

import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.ApplicationUseCases
import com.linku.data.usecase.EmojiUseCases
import com.linku.data.usecase.MessageUseCases
import com.linku.data.usecase.SettingUseCase
import com.linku.domain.Authenticator
import com.linku.domain.Resource
import com.linku.domain.eventOf
import com.linku.im.network.ConnectivityObserver
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LinkUViewModel @Inject constructor(
    private val messageUseCases: MessageUseCases,
    private val emojiUseCases: EmojiUseCases,
    private val applicationUseCases: ApplicationUseCases,
    private val settings: SettingUseCase,
    connectivityObserver: ConnectivityObserver,
    val authenticator: Authenticator
) : BaseViewModel<LinkUState, LinkUEvent>(LinkUState()) {
    init {
        onEvent(LinkUEvent.InitConfig)
        connectivityObserver.observe()
            .onEach { state ->
                when (state) {
                    ConnectivityObserver.State.Available -> {
                        onMessage("网络连接已恢复")
                        initSessionJob?.cancel()
                        initSessionJob = authenticator.observeCurrent
                            .distinctUntilChanged()
                            .onEach { userId ->
                                if (userId != null) onEvent(LinkUEvent.InitSession)
                                else onEvent(LinkUEvent.Disconnect)
                            }
                            .launchIn(viewModelScope)
                    }
                    ConnectivityObserver.State.Unavailable -> {
                        onMessage("未连接到网络")
                    }
                    ConnectivityObserver.State.Losing -> {
                        onMessage("网络连接断开中")
                        initSessionJob?.cancel()
                    }
                    ConnectivityObserver.State.Lost -> {
                        onMessage("网络连接已断开")
                    }
                }
            }
            .launchIn(viewModelScope)

    }

    override fun onEvent(event: LinkUEvent) {
        when (event) {
            LinkUEvent.InitSession -> initSession()
            LinkUEvent.InitConfig -> initConfig()
            LinkUEvent.PopBackStack -> writable = readable.copy(
                navigateUp = eventOf(Unit)
            )
            LinkUEvent.ToggleDarkMode -> {
                val saved = !readable.isDarkMode
                settings.isDarkMode = saved
                writable = readable.copy(
                    isDarkMode = saved
                )
            }
            LinkUEvent.Disconnect -> {
                updateLabel(Label.NoAuth)
                viewModelScope.launch { messageUseCases.closeSession() }
            }
            is LinkUEvent.Navigate -> {
                writable = readable.copy(
                    navigate = eventOf(event.screen.route)
                )
            }
            is LinkUEvent.NavigateWithArgs -> {
                writable = readable.copy(
                    navigate = eventOf(event.route)
                )
            }
            LinkUEvent.ToggleDynamic -> {
                val saved = !readable.dynamicEnabled
                settings.isDynamicMode = saved
                writable = readable.copy(
                    dynamicEnabled = saved
                )
            }
        }
    }

    private sealed class Label {
        object Default : Label()
        object Connecting : Label()
        object ConnectedFailed : Label()
        object NoAuth : Label()
    }

    private fun updateLabel(label: Label) {
        writable = readable.copy(
            label = when (label) {
                Label.Default -> applicationUseCases.getString(R.string.app_name)
                Label.Connecting -> applicationUseCases.getString(R.string.connecting)
                Label.ConnectedFailed -> applicationUseCases.getString(R.string.connected_failed)
                Label.NoAuth -> applicationUseCases.getString(R.string.no_auth)
            }
        )
    }

    private var initSessionJob: Job? = null
    private var times = 0
    private fun initSession() {
        settings.debug {
            times++
            applicationUseCases.toast("Init session, times: $times")
        }
        initSessionJob?.cancel()
        initSessionJob = viewModelScope.launch {
            messageUseCases
                .initSession(authenticator.currentUID)
                .collectLatest { resource ->
                    when (resource) {
                        Resource.Loading -> {
                            updateLabel(Label.Connecting)
                        }
                        is Resource.Success -> {
                            updateLabel(Label.Default)
                        }
                        is Resource.Failure -> {
                            updateLabel(Label.ConnectedFailed)
                            launch {
                                delay(3000)
                                settings.debug {
                                    applicationUseCases.toast("InitSession Failed: ${resource.message}")
                                }
                                onEvent(LinkUEvent.InitSession)
                            }
                        }
                    }
                }
        }

    }

    private fun initConfig() {
        val isDarkMode = settings.isDarkMode
        val enableDynamic = settings.isDynamicMode
        emojiUseCases.initialize()
            .onEach { resource ->
                when (resource) {
                    Resource.Loading -> {}
                    is Resource.Success -> {
                        writable = readable.copy(
                            isDarkMode = isDarkMode,
                            dynamicEnabled = enableDynamic,
                            isReady = true
                        )
                    }
                    is Resource.Failure -> {
                        writable = readable.copy(
                            isDarkMode = isDarkMode,
                            dynamicEnabled = enableDynamic,
                            isReady = true,
                        )
                        onMessage(resource.message)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

}