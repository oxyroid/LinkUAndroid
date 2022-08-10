package com.linku.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.linku.domain.room.converter.ConversationTypeConverter
import com.linku.domain.room.converter.IntListConverter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@TypeConverters(IntListConverter::class, ConversationTypeConverter::class)
data class Conversation(
    @PrimaryKey
    val id: Int,
    val type: Type,
    val updatedAt: Long = 0L,
    val name: String = "",
    val avatar: String = "",
    val owner: Int,
    val member: List<Int> = emptyList(),
    val description: String = ""
) {

    sealed class Type(val type: Int) {
        object PM : Type(0)
        object GROUP : Type(1)
        object BANNED : Type(2)
        object UNKNOWN : Type(-1)

        override fun toString(): String = type.toString()

        companion object {
            fun parse(type: Int) = when (type) {
                0 -> PM
                1 -> GROUP
                2 -> BANNED
                else -> UNKNOWN
            }
        }
    }
}

@Serializable
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
    type = Conversation.Type.parse(type),
    updatedAt = updatedAt,
    name = name,
    avatar = avatar,
    owner = owner,
    member = member,
    description = description
)