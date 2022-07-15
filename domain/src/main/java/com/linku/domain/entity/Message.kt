package com.linku.domain.entity

import androidx.annotation.Keep
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
    open val timestamp: Long,
    open val uuid: String,
    open val sendState: Int
) {

    companion object {
        const val STATE_PENDING = 0
        const val STATE_SEND = 1
        const val STATE_FAILED = 2
        const val STATE_ANOTHER = 3
        val DEBUG = Message(
            0, 0, 0, "This is Thxbrop!", "text", 0, "", STATE_SEND
        )
    }

    fun toReadable(): Message = when (type) {
        "text" -> TextMessage(id, cid, uid, content, timestamp, uuid, sendState)
        "image" -> ImageMessage(id, cid, uid, content, timestamp, uuid, sendState)
        else -> this
    }
}

data class TextMessage(
    override val id: Int,
    override val cid: Int,
    override val uid: Int,
    val text: String,
    override val timestamp: Long,
    override val uuid: String,
    override val sendState: Int
) : Message(id, cid, uid, text, "text", timestamp, uuid, sendState)

data class ImageMessage(
    override val id: Int,
    override val cid: Int,
    override val uid: Int,
    val url: String,
    override val timestamp: Long,
    override val uuid: String,
    override val sendState: Int
) : Message(id, cid, uid, url, "image", timestamp, uuid, sendState)

@Serializable
@Keep
data class MessageDTO(
    @PrimaryKey @SerialName("id") val id: Int = -1,
    @SerialName("cid") val cid: Int = -1,
    @SerialName("uid") val uid: Int = -1,
    @SerialName("tid") val tid: Int = -1,
    @SerialName("content") val content: String = "",
    @SerialName("type") val type: String = "",
    @SerialName("send_time") val timestamp: Long = System.currentTimeMillis(),
    @SerialName("uuid") val uuid: String = ""
) {
    private val sendState: Int = when {
        uuid.isBlank() -> Message.STATE_ANOTHER
        else -> Message.STATE_SEND
    }

    fun toMessage() = when (type.lowercase().trim()) {
        "text" -> TextMessage(id, cid, uid, content, timestamp, uuid, sendState)
        "image" -> ImageMessage(id, cid, uid, content, timestamp, uuid, sendState)
        else -> Message(id, cid, uid, content, type, timestamp, uuid, sendState)
    }
}