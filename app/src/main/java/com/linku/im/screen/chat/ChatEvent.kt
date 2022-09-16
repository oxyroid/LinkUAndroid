package com.linku.im.screen.chat

import android.net.Uri
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.text.input.TextFieldValue

sealed class ChatEvent {
    data class Initialize(val cid: Int) : ChatEvent()
    object Syncing : ChatEvent()
    object SendMessage : ChatEvent()
    data class TextChange(val text: TextFieldValue) : ChatEvent()
    data class EmojiChange(val emoji: String) : ChatEvent()
    data class OnScroll(val index: Int, val offset: Int) : ChatEvent()
    data class OnFileUriChange(val uri: Uri?) : ChatEvent()
    data class Expanded(val value: Boolean) : ChatEvent()
    data class Reply(val mid: Int?) : ChatEvent()
    object ReadAll : ChatEvent()
    data class Preview(val preview: ChatState.Preview) : ChatEvent()
    object DismissPreview : ChatEvent()
    data class FocusMessage(val mid: Int) : ChatEvent()
    data class ResendMessage(val mid: Int) : ChatEvent()
    data class CancelMessage(val mid: Int) : ChatEvent()

    object LoseFocusMessage : ChatEvent()
}
