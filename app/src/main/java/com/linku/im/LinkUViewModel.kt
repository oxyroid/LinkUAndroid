package com.linku.im

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.EmojiUseCases
import com.linku.data.usecase.MessageUseCases
import com.linku.domain.Authenticator
import com.linku.domain.Resource
import com.linku.domain.eventOf
import com.linku.im.Constants.SAVED_DARK_MODE
import com.linku.im.Constants.SAVED_DYNAMIC_MODE
import com.linku.im.extension.TAG
import com.linku.im.extension.debug
import com.linku.im.screen.BaseViewModel
import com.tencent.mmkv.MMKV
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LinkUViewModel @Inject constructor(
    private val messageUseCases: MessageUseCases,
    private val emojiUseCases: EmojiUseCases
) : BaseViewModel<LinkUState, LinkUEvent>(LinkUState()) {
    init {
        onEvent(LinkUEvent.InitConfig)
    }

    override fun onEvent(event: LinkUEvent) {
        when (event) {
            LinkUEvent.InitSession -> initSession()
            LinkUEvent.InitConfig -> initConfig()
            LinkUEvent.PopBackStack -> _state.value = readable.copy(
                navigateUp = eventOf(Unit)
            )

            LinkUEvent.ToggleDarkMode -> {
                MMKV.defaultMMKV().encode(SAVED_DARK_MODE, !readable.isDarkMode)
                _state.value = readable.copy(
                    isDarkMode = !readable.isDarkMode
                )
            }

            LinkUEvent.Disconnect -> {
                updateLabel(Label.NoAuth)
                viewModelScope.launch { messageUseCases.closeSession() }
            }
            is LinkUEvent.Navigate -> {
                _state.value = readable.copy(
                    navigate = eventOf(event.screen.route)
                )
            }

            is LinkUEvent.NavigateWithArgs -> {
                _state.value = readable.copy(
                    navigate = eventOf(event.route)
                )
            }
            LinkUEvent.ToggleDynamic -> {
                MMKV.defaultMMKV().encode(SAVED_DYNAMIC_MODE, !readable.dynamicEnabled)
                _state.value = readable.copy(
                    dynamicEnabled = !state.value.dynamicEnabled
                )
            }
        }
    }

    private sealed class Label(val text: String) {
        object Default : Label(applicationContext.getString(R.string.app_name))
        object Connecting : Label(applicationContext.getString(R.string.connecting))
        object ConnectedFailed : Label(applicationContext.getString(R.string.connected_failed))
        object NoAuth : Label(applicationContext.getString(R.string.no_auth))
    }

    private fun updateLabel(label: Label) {
        _state.value = readable.copy(
            label = label.text
        )
    }

    private var initSessionJob: Job? = null
    private fun initSession() {
        initSessionJob?.cancel()
        initSessionJob = viewModelScope.launch {
            val userId = Authenticator.currentUID
            checkNotNull(userId)
            messageUseCases.initSession(userId)
                .onEach { resource ->
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
                                debug { Log.v(TAG, "Retrying...") }
                                onEvent(LinkUEvent.InitSession)
                            }
                        }
                    }
                }
                .launchIn(viewModelScope)

        }
    }

    private fun initConfig() {
        val isDarkMode = MMKV.defaultMMKV().getBoolean(SAVED_DARK_MODE, true)
        val enableDynamic =
            MMKV.defaultMMKV().getBoolean(SAVED_DYNAMIC_MODE, false)
        emojiUseCases.initialize()
            .onEach { resource ->
                when (resource) {
                    Resource.Loading -> {}
                    is Resource.Success -> {
                        _state.value = readable.copy(
                            isDarkMode = isDarkMode,
                            dynamicEnabled = enableDynamic,
                            isReady = true
                        )
                    }
                    is Resource.Failure -> {
                        _state.value = readable.copy(
                            isDarkMode = isDarkMode,
                            dynamicEnabled = enableDynamic,
                            isReady = true
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

}