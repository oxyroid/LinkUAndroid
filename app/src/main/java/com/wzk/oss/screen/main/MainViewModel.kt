package com.wzk.oss.screen.main

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.wzk.domain.entity.Conversation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    val state = mutableStateOf(MainState(
        conversations = buildList {
            repeat(20) {
                Conversation(
                    id = it,
                    updatedAt = it.toLong(),
                    name = "test$it",
                    avatar = "",
                    member = emptyList(),
                    owner = it,
                    description = "description$it"
                ).also(::add)
            }
        }
    ))
}