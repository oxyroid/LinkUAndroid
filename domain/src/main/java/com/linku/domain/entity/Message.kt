package com.linku.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.linku.domain.extension.json
import com.linku.domain.room.converter.MessageTypeConverter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

@Entity
@TypeConverters(MessageTypeConverter::class)
open class Message(
    @PrimaryKey open val id: Int,
    open val cid: Int,
    open val uid: Int,
    open val content: String,
    val type: Type,
    open val timestamp: Long,
    open val uuid: String,
    open val sendState: Int
) {
    sealed class Type(private val text: String) {
        object Text : Type("text")
        object Image : Type("image")
        object Graphics : Type("graphics")
        object Unknown : Type("")

        companion object {
            fun parse(text: String) = when (text.lowercase().trim()) {
                "text" -> Text
                "image" -> Image
                "graphics" -> Graphics
                else -> Unknown
            }
        }

        override fun toString(): String = text
    }

    companion object {
        const val STATE_PENDING = 0
        const val STATE_SEND = 1
        const val STATE_FAILED = 2
        const val STATE_ANOTHER = 3
    }

    fun toReadable(): Message = when (this) {
        is TextMessage, is ImageMessage, is GraphicsMessage -> this
        else -> when (type) {
            Type.Text -> TextMessage(id, cid, uid, content, timestamp, uuid, sendState)
            Type.Image -> ImageMessage(id, cid, uid, content, timestamp, uuid, sendState)
            Type.Graphics -> {
                val graphicsContent = json.decodeFromString<GraphicsContent>(content)
                GraphicsMessage(
                    id = id,
                    cid = cid,
                    uid = uid,
                    text = graphicsContent.text,
                    url = graphicsContent.url,
                    timestamp = timestamp,
                    uuid = uuid,
                    sendState = sendState
                )
            }
            else -> this
        }
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
) : Message(id, cid, uid, text, Type.Text, timestamp, uuid, sendState)

data class ImageMessage(
    override val id: Int,
    override val cid: Int,
    override val uid: Int,
    val url: String,
    override val timestamp: Long,
    override val uuid: String,
    override val sendState: Int
) : Message(id, cid, uid, url, Type.Image, timestamp, uuid, sendState)

data class GraphicsMessage(
    override val id: Int,
    override val cid: Int,
    override val uid: Int,
    val text: String,
    val url: String,
    override val timestamp: Long,
    override val uuid: String,
    override val sendState: Int
) : Message(
    id = id,
    cid = cid,
    uid = uid,
    content = GraphicsContent(text, url).let(json::encodeToString),
    type = Type.Image,
    timestamp = timestamp,
    uuid = uuid,
    sendState = sendState
)

@Serializable
data class MessageDTO(
    @SerialName("id") val id: Int = -1,
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

    fun toMessage() = when (Message.Type.parse(type)) {
        Message.Type.Text -> TextMessage(id, cid, uid, content, timestamp, uuid, sendState)
        Message.Type.Image -> ImageMessage(id, cid, uid, content, timestamp, uuid, sendState)
        else -> Message(
            id = id,
            cid = cid,
            uid = uid,
            content = content,
            type = Message.Type.parse(type),
            timestamp = timestamp,
            uuid = uuid,
            sendState = sendState
        )
    }
}

@Serializable
data class GraphicsContent(
    @SerialName("text")
    val text: String,
    @SerialName("url")
    val url: String
)
