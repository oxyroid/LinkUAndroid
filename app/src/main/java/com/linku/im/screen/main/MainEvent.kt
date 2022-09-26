package com.linku.im.screen.main

sealed class MainEvent {
    object ObserveConversations : MainEvent()
    object UnsubscribeConversations : MainEvent()
    object FetchConversations : MainEvent()
}