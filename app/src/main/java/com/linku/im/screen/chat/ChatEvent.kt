package com.linku.im.screen.chat

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue

sealed class ChatEvent {
    data class Initialize(val cid: Int) : ChatEvent()
    object SendMessage : ChatEvent()
    data class TextChange(val text: TextFieldValue) : ChatEvent()
    data class EmojiChange(val emoji: String) : ChatEvent()
    data class OnScroll(val index: Int, val offset: Int) : ChatEvent()
    data class OnFileUriChange(val uri: Uri?) : ChatEvent()
    data class Expanded(val value: Boolean) : ChatEvent()
    data class Reply(val mid: Int?) : ChatEvent()
    object ReadAll : ChatEvent()
    data class ShowImage(val img: String) : ChatEvent()
    object DismissImage : ChatEvent()
}
