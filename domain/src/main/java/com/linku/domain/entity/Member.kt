package com.linku.domain.entity

import androidx.room.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(primaryKeys = ["cid", "uid"])
@Serializable
data class Member(
    @SerialName("cid")
    val cid: Int,
    @SerialName("uid")
    val uid: Int,
    @SerialName("conv_nickname")
    val name: String? = null,
    @SerialName("is_admin")
    val root: Boolean
)
