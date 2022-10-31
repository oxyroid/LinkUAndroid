package com.linku.domain.entity

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Stable
data class Conversation(
    @PrimaryKey
    val id: Int,
    val type: Type,
    val updatedAt: Long = 0L,
    val name: String = "",
    val avatar: String = "",
    val owner: Int,
    val member: List<Int> = emptyList(),
    val description: String = "",
    val pinned: Boolean
) {

    sealed class Type(val type: Int) {
        object PM : Type(0)
        object GROUP : Type(1)
        object BANNED : Type(2)
        object UNKNOWN : Type(-1)

        override fun toString(): String = type.toString()


        @Suppress("unused")
        object Converter {
            @TypeConverter
            fun decode(value: Int?): Type {
                return when (value) {
                    0 -> PM
                    1 -> GROUP
                    2 -> BANNED
                    else -> UNKNOWN
                }
            }

            @TypeConverter
            fun encode(type: Type): Int {
                return type.type
            }
        }
    }
}

@Serializable
@Stable
data class ConversationDTO(
    @SerialName("id")
    val id: Int,
    @SerialName("last_updated")
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
    val description: String = "",
    @SerialName("type")
    val type: Int
)

fun ConversationDTO.toConversation() = Conversation(
    id = id,
    type = Conversation.Type.Converter.decode(type),
    updatedAt = updatedAt,
    name = name,
    avatar = avatar,
    owner = owner,
    member = member,
    description = description,
    pinned = false
)