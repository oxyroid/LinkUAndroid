package com.linku.im.screen.chat

import android.net.Uri

sealed class ChatEvent {
    data class Initial(val cid: Int) : ChatEvent()
    object SendMessage : ChatEvent()
    data class TextChange(val text: String) : ChatEvent()
    data class EmojiChange(val emoji: String) : ChatEvent()
    data class FirstVisibleIndex(val index: Int) : ChatEvent()
    data class OnFileUriChange(val uri: Uri?) : ChatEvent()
    data class Expanded(val value: Boolean) : ChatEvent()

    object ReadAll : ChatEvent()
}
