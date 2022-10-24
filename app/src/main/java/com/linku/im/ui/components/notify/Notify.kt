package com.linku.im.ui.components.notify

import androidx.compose.ui.text.AnnotatedString

data class Notify(
    val message: AnnotatedString,
    val mode: NotifyMode = NotifyMode.Duration.Medium,
    val level: NotifyLevel = NotifyLevel.Common,
    val action: NotifyAction? = null
)
