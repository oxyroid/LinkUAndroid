package com.linku.im.ui.components.notify

data class NotifyAction(
    val text: String,
    val onClick: () -> Unit
)
