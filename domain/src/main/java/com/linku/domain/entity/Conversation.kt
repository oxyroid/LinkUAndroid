package com.linku.domain.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.linku.domain.room.converter.IntListConverter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@TypeConverters(IntListConverter::class)
@Serializable
@Keep
data class Conversation(
    @PrimaryKey
    @SerialName("id")
    val id: Int,
    @SerialName("updatedAt")
    val updatedAt: Long = 0L,
    @SerialName("name")
    val name: String = "",
    @SerialName("avatar")
    val avatar: String = "",
    @SerialName("owner")
    val owner: Int,
    @SerialName("member")
    val member: List<Int> = emptyList(),
    @SerialName("description")
    val description: String = ""
)