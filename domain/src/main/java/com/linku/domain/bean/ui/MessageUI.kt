package com.linku.domain.bean.ui

import androidx.annotation.Keep
import androidx.compose.runtime.Stable
import com.linku.domain.bean.Bubble
import com.linku.domain.entity.Message

@Keep
@Stable
data class MessageUI(
    val message: Message,
    val config: Bubble
)
