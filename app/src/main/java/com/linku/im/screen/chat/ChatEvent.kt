package com.linku.im.screen.chat

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue

sealed class ChatEvent {
    data class FetchChannel(val cid: Int) : ChatEvent()
    object ObserveMessage : ChatEvent()
    object FetchChannelDetail : ChatEvent()
    object SendMessage : ChatEvent()
    data class OnTextChange(val text: TextFieldValue) : ChatEvent()
    data class OnEmoji(val emoji: String) : ChatEvent()
    data class OnFile(val uri: Uri?) : ChatEvent()
    data class OnEmojiSpanExpanded(val value: Boolean) : ChatEvent()
    data class OnReply(val mid: Int?) : ChatEvent()
    object ReadAll : ChatEvent()
    object PushShortcut : ChatEvent()
    data class OnFocus(val mid: Int?) : ChatEvent()
    data class ResendMessage(val mid: Int) : ChatEvent()
    data class CancelMessage(val mid: Int) : ChatEvent()

    data class Forward(val mode: ChatMode) : ChatEvent()
    object Remain : ChatEvent()
    data class RemainIf(val block: () -> Boolean) : ChatEvent()
    object ResetNode: ChatEvent()
}
