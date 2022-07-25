package com.linku.im.screen.chat.vo

import androidx.annotation.Keep
import com.linku.domain.entity.Message

@Keep
data class MessageVO(
    val content: String,
    val isAnother: Boolean,
    val sendState: Int,
    val timestamp: Long,
    val avatar: String? = null,
    val name: String? = null,
    val isShowName: Boolean
)

fun Message.toMessageVO(
    content: String = this.content,
    isAnother: Boolean,
    avatar: String?,
    name: String?,
    isShowName: Boolean
): MessageVO {
    return MessageVO(content, isAnother, sendState, timestamp, avatar, name, isShowName)
}