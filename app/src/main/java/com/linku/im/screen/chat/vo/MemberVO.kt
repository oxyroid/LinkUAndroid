package com.linku.im.screen.chat.vo

import androidx.compose.runtime.Stable

@Stable
data class MemberVO(
    val cid: Int,
    val uid: Int,
    val username: String,
    val avatar: String,
    val admin: Boolean,
    val memberName: String
)

