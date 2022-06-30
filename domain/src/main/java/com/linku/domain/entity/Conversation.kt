package com.linku.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Conversation(
    @PrimaryKey val id: Int,
    val updatedAt: Long,
    val name: String,
    val avatar: String,
    val owner: Int,
    val member: List<Int>,
    val description: String
)

@Serializable
data class ConversationSimplify(
    val id: Int,
    val updatedAt: Long
)