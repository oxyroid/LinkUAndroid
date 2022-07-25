package com.linku.im.screen.chat

import android.net.Uri

sealed class ChatEvent {
    data class Initial(val cid: Int) : ChatEvent()
    object SendMessage : ChatEvent()
    data class TextChange(val text: String) : ChatEvent()
    data class FirstVisibleIndex(val index: Int) : ChatEvent()
    data class OnFileUriChange(val uri: Uri?) : ChatEvent()
    object ReadAll : ChatEvent()
}
