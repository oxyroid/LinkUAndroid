package com.wzk.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Conversation(
    @PrimaryKey val id: Int,
    val name: String
)
