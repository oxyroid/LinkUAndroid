package com.linku.im.screen.chat.vo

data class MemberVO(
    val cid: Int,
    val uid: Int,
    val username: String,
    val avatar: String,
    val admin: Boolean,
    val memberName: String
)

