package com.linku.im.screen.introduce

import com.linku.domain.Event
import com.linku.im.screen.introduce.composable.Property

data class IntroduceState(
    val loading: Boolean = false,
    val verifiedEmailStarting: Boolean = false,
    val verifiedEmailDialogShowing: Boolean = false,
    val verifiedEmailCodeVerifying: Boolean = false,
    val actionsLabel: String = "",
    val actions: List<Property.Data.Action> = emptyList(),
    val error: Event<String> = Event.Handled(),
    val logout: Boolean = false,
    val dataProperties: List<Property> = emptyList(),
    val settingsProperties: List<Property> = emptyList()
)
