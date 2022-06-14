package com.wzk.oss.screen.profile.dto

data class UserDetail(
    val id: Int,
    val name: String,
    val nickName: String,
    val img: String?,
    val description: String,
    val mobilePhone: String?,
    val lastOnlineAt: Long
)
