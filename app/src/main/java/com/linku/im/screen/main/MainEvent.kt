package com.linku.im.screen.main

import androidx.compose.ui.text.input.TextFieldValue

sealed interface MainEvent {
    object ObserveConversations : MainEvent
    object UnsubscribeConversations : MainEvent
    data class Pin(val cid: Int) : MainEvent

    data class Forward(val mode: MainMode) : MainEvent
    object Remain : MainEvent
    object FetchNotifications : MainEvent
    object Query : MainEvent
    data class OnQueryText(val text: TextFieldValue) : MainEvent
    object ToggleQueryIncludeDescription : MainEvent
    object ToggleQueryIncludeEmail : MainEvent
}
