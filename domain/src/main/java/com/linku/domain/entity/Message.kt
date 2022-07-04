package com.linku.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
open class Message(
    @PrimaryKey open val id: Int? = null,
    open val cid: Int,
    open val uid: Int,
    open val content: String,
    val type: String = "text",
    open val timestamp: Long
) {
    fun toReadable(): Message = when (type) {
        "text" -> TextMessage(id, cid, uid, content, timestamp)
        "image" -> ImageMessage(id, cid, uid, content, timestamp)
        else -> this
    }
}

data class TextMessage(
    override val id: Int? = null,
    override val cid: Int,
    override val uid: Int,
    val text: String,
    override val timestamp: Long
) : Message(id, cid, uid, text, "text", timestamp)

data class ImageMessage(
    override val id: Int? = null,
    override val cid: Int,
    override val uid: Int,
    val url: String,
    override val timestamp: Long
) : Message(id, cid, uid, url, "image", timestamp)

data class MessageDTO(
    @PrimaryKey val id: Int? = null,
    val cid: Int,
    val uid: Int,
    val content: String,
    @SerialName("send_time")
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMessage() = Message(
        id = id,
        cid = cid,
        uid = uid,
        content = content,
        type = "text",
        timestamp = timestamp
    ).toReadable()
}