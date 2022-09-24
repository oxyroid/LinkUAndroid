package com.linku.im.screen.chat.vo

import androidx.annotation.Keep
import androidx.compose.runtime.Stable
import com.linku.domain.entity.Message
import com.linku.im.screen.chat.composable.BubbleConfig


@Keep
@Stable
data class MessageVO(
    val message: Message,
    val config: BubbleConfig
)