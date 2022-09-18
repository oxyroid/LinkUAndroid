package com.linku.im.screen.chat

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue

sealed class ChatEvent {
    data class Initialize(val cid: Int) : ChatEvent()
    object Syncing : ChatEvent()
    object SendMessage : ChatEvent()
    data class OnTextChange(val text: TextFieldValue) : ChatEvent()
    data class OnEmoji(val emoji: String) : ChatEvent()
    data class OnScroll(val index: Int, val offset: Int) : ChatEvent()
    data class OnFile(val uri: Uri?) : ChatEvent()
    data class OnExpanded(val value: Boolean) : ChatEvent()
    data class OnReply(val mid: Int?) : ChatEvent()
    data class OnPreview(val preview: String?) : ChatEvent()
    object ReadAll : ChatEvent()
    data class OnFocus(val mid: Int?) : ChatEvent()
    data class ResendMessage(val mid: Int) : ChatEvent()
    data class CancelMessage(val mid: Int) : ChatEvent()
}
