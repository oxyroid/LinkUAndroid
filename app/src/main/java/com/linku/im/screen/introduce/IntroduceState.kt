package com.linku.im.screen.introduce

import com.linku.domain.Event
import com.linku.im.screen.introduce.composable.Property

data class IntroduceState(
    val uploading: Boolean = false,
    val isOthers: Boolean = false,
    val uid: Int = -1,
    val verifiedEmailStarting: Boolean = false,
    val verifiedEmailDialogShowing: Boolean = false,
    val verifiedEmailCodeVerifying: Boolean = false,
    val actionsLabel: String = "",
    val actions: List<Property.Data.Action> = emptyList(),
    val verifiedEmailCodeMessage: String = "",
    val logout: Boolean = false,
    val dataProperties: List<Property> = emptyList(),
    val settingsProperties: List<Property> = emptyList(),
    val editEvent: Event<IntroduceEvent.Edit.Type> = Event.Handled(),
    val avatar: String = "",
    val preview: String = "",
    val runLauncher: Event<Unit> = Event.Handled()
)
