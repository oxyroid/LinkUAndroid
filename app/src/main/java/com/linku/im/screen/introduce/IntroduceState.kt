package com.linku.im.screen.introduce

import com.linku.core.wrapper.Event
import com.linku.im.screen.introduce.composable.Property

data class IntroduceState(
    val uploading: Boolean = false,
    val category: Category = Category.Personal,
    val uid: Int = -1,
    val verifiedEmailStarting: Boolean = false,
    val verifiedEmailDialogShowing: Boolean = false,
    val verifiedEmailCodeVerifying: Boolean = false,
    val actionsLabel: String = "",
    val actions: List<Property.Data.Action> = emptyList(),
    val verifiedEmailCodeMessage: String = "",
    val dataProperties: List<Property> = emptyList(),
    val settingsProperties: List<Property> = emptyList(),
    val editEvent: Event<IntroduceEvent.Edit.Type> = Event.Handled(),
    val avatar: String = "",
    val runLauncher: Event<Unit> = Event.Handled(),
    val goChat: Event<Int> = Event.Handled()
)

sealed class Category {
    object Personal : Category()
    data class User(
        val friendship: Friendship = Friendship.Loading
    ) : Category()
}

sealed class Friendship {
    object None : Friendship()
    data class Pending(val isReceived: Boolean) : Friendship()
    data class Completed(val cid: Int) : Friendship()

    object Loading : Friendship()
}
