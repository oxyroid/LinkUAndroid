package com.linku.im.screen.main

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.linku.domain.entity.Conversation
import com.linku.im.BuildConfig
import com.linku.im.extension.debug
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    val state = mutableStateOf(
        MainState(
            conversations = buildList {
                debug {
                    Conversation(
                        id = 1,
                        updatedAt = 1,
                        name = "构筑版本",
                        avatar = "",
                        member = emptyList(),
                        owner = 1,
                        description = BuildConfig.VERSION_NAME
                    ).also(::add)
                    Conversation(
                        id = 1,
                        updatedAt = 1,
                        name = "UI调整",
                        avatar = "",
                        member = emptyList(),
                        owner = 1,
                        description = "配色和布局再次微调"
                    ).also(::add)
                    Conversation(
                        id = 1,
                        updatedAt = 1,
                        name = "新功能测试",
                        avatar = "",
                        member = emptyList(),
                        owner = 1,
                        description = "启动动画：仅限Android12及以上设备"
                    ).also(::add)
                    Conversation(
                        id = 1,
                        updatedAt = 1,
                        name = "重构",
                        avatar = "",
                        member = emptyList(),
                        owner = 1,
                        description = "导航重构，现在应用共用一个AppBar"
                    ).also(::add)
                }
            }
        )
    )
}