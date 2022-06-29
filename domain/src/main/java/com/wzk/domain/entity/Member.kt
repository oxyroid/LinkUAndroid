package com.wzk.domain.entity

import androidx.room.Entity

@Entity(primaryKeys = ["cid", "uid"])
data class Member(
    val cid: Int,
    val uid: Int,
    val name: String,
    val root: Boolean
)
