package com.linku.im.screen.main

sealed class MainMode {
    object Conversations : MainMode()
    object Notifications : MainMode()
    object NewChannel : MainMode()
}
