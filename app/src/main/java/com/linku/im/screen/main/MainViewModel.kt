package com.linku.im.screen.main

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linku.domain.entity.Conversation
import com.linku.im.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    val state = mutableStateOf(MainState(loading = true))

    init {
        viewModelScope.launch {
            delay(1200)
            state.value = MainState(
                conversations = buildList {
                    Conversation(
                        id = 1,
                        updatedAt = 1,
                        name = "构筑版本",
                        avatar = "",
                        member = emptyList(),
                        owner = 1,
                        description = BuildConfig.VERSION_NAME
                    ).also(::add)

                }
            )
        }

    }
}