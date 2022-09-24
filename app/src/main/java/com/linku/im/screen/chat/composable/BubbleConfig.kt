package com.linku.im.screen.chat.composable

import androidx.compose.runtime.Stable
import com.linku.domain.entity.Message

sealed class BubbleConfig(
    open val isAnother: Boolean,
    open val isShowTime: Boolean,
    open val sendState: Int,
    open val isEndOfGroup: Boolean,
    open val reply: ReplyConfig?
) {
    @Stable
    data class PM(
        override val sendState: Int = Message.STATE_SEND,
        private val another: Boolean = false,
        override val isShowTime: Boolean = false,
        override val isEndOfGroup: Boolean = false,
        override val reply: ReplyConfig? = null
    ) : BubbleConfig(another, isShowTime, sendState, isEndOfGroup, reply)

    @Stable
    data class Group(
        override val sendState: Int = Message.STATE_SEND,
        private val other: Boolean = false,
        override val isShowTime: Boolean = false,
        val avatarVisibility: Boolean = false,
        val nameVisibility: Boolean = false,
        val name: String = "",
        val avatar: String = "",
        override val isEndOfGroup: Boolean = false,
        override val reply: ReplyConfig? = null
    ) : BubbleConfig(other, isShowTime, sendState, isEndOfGroup, reply)
}

@Stable
data class ReplyConfig(
    val repliedMid: Int,
    val index: Int,
    val display: String
)