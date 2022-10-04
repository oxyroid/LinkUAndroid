package com.linku.domain.bean

import androidx.annotation.Keep
import androidx.compose.runtime.Stable
import com.linku.domain.entity.Message

@Keep
@Stable
data class MessageVO(
    val message: Message,
    val config: Bubble
)
