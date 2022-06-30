package com.wzk.domain.entity

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Entity(primaryKeys = ["cid", "uid"])
@Serializable
data class Member(
    val cid: Int,
    val uid: Int,
    val name: String,
    val root: Boolean
)
