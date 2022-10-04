package com.linku.domain.bean

import androidx.compose.runtime.Stable
import com.linku.domain.entity.Message

sealed class Bubble(
    open val isAnother: Boolean,
    open val isShowTime: Boolean,
    open val sendState: Int,
    open val isEndOfGroup: Boolean,
    open val reply: Reply?
) {
    @Stable
    data class PM(
        override val sendState: Int = Message.STATE_SEND,
        private val another: Boolean = false,
        override val isShowTime: Boolean = false,
        override val isEndOfGroup: Boolean = false,
        override val reply: Reply? = null
    ) : Bubble(another, isShowTime, sendState, isEndOfGroup, reply)

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
        override val reply: Reply? = null
    ) : Bubble(other, isShowTime, sendState, isEndOfGroup, reply)
}

@Stable
data class Reply(
    val repliedMid: Int,
    val index: Int,
    val display: String
)
