package com.linku.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.linku.domain.entity.Conversation.Companion.TYPE_PM
import com.linku.domain.room.converter.IntListConverter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@TypeConverters(IntListConverter::class)
data class Conversation(
    @PrimaryKey
    val id: Int,
    val type: Int,
    val updatedAt: Long = 0L,
    val name: String = "",
    val avatar: String = "",
    val owner: Int,
    val member: List<Int> = emptyList(),
    val description: String = ""
) {
    companion object {
        const val TYPE_PM = 0
        const val TYPE_GROUP = 1
        const val TYPE_BANNED = -1
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
    val type: Int = TYPE_PM
)

fun ConversationDTO.toConversation() = Conversation(
    id = id,
    type = type,
    updatedAt = updatedAt,
    name = name,
    avatar = avatar,
    owner = owner,
    member = member,
    description = description
)