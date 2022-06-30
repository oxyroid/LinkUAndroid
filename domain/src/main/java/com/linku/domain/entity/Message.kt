package com.linku.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
open class Message(
    @PrimaryKey open val id: Int? = null,
    open val cid: Int,
    open val uid: Int,
    open val content: String,
    private val type: String
) {
    fun toReadable(): Message = when (type) {
        "text" -> TextMessage(id, cid, uid, content)
        "image" -> ImageMessage(id, cid, uid, content)
        else -> this
    }
}

data class TextMessage(
    override val id: Int? = null,
    override val cid: Int,
    override val uid: Int,
    val text: String
) : Message(id, cid, uid, text, "text")

data class ImageMessage(
    override val id: Int? = null,
    override val cid: Int,
    override val uid: Int,
    val url: String
) : Message(id, cid, uid, url, "image")


