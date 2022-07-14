package com.linku.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
open class Message(
    @PrimaryKey open val id: Int,
    open val cid: Int,
    open val uid: Int,
    open val content: String,
    val type: String,
    @SerialName("send_time")
    open val timestamp: Long
) {

    companion object {
        val DEBUG = Message(0, 0, 0, "This is Thxbrop!", "text", 0)
    }

    fun toReadable(): Message = when (type) {
        "text" -> TextMessage(id, cid, uid, content, timestamp)
        "image" -> ImageMessage(id, cid, uid, content, timestamp)
        else -> this
    }
}

data class TextMessage(
    override val id: Int,
    override val cid: Int,
    override val uid: Int,
    val text: String,
    override val timestamp: Long
) : Message(id, cid, uid, text, "text", timestamp)

data class ImageMessage(
    override val id: Int,
    override val cid: Int,
    override val uid: Int,
    val url: String,
    override val timestamp: Long
) : Message(id, cid, uid, url, "image", timestamp)

@Serializable
data class MessageDTO(
    @PrimaryKey val id: Int,
    val cid: Int,
    val uid: Int,
    val tid: Int,
    val content: String,
    val type: String,
    @SerialName("send_time")
    val timestamp: Long = System.currentTimeMillis(),
    @SerialName("send_state")
    val state: Int
) {
    fun toMessage() = when (type) {
        "text" -> TextMessage(id, cid, uid, content, timestamp)
        "image" -> ImageMessage(id, cid, uid, content, timestamp)
        else -> Message(id, cid, uid, content, type, timestamp)
    }
}