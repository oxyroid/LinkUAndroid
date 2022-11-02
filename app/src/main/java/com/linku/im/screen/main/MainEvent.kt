package com.linku.im.screen.main

sealed class MainEvent {
    object ObserveConversations : MainEvent()
    object UnsubscribeConversations : MainEvent()
    data class Pin(val cid: Int) : MainEvent()

    data class Forward(val mode: MainMode) : MainEvent()
    object Remain : MainEvent()
    object FetchNotifications : MainEvent()
}
