package com.linku.domain.entity

import androidx.annotation.Keep
import androidx.room.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(primaryKeys = ["cid", "uid"])
@Serializable
@Keep
data class Member(
    @SerialName("cid")
    val cid: Int,
    @SerialName("uid")
    val uid: Int,
    @SerialName("name")
    val name: String,
    @SerialName("root")
    val root: Boolean
)
