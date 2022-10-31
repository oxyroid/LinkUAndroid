package com.linku.domain.entity

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.linku.core.extension.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

@Entity
@Stable
open class Message(
    @PrimaryKey open val id: Int,
    open val cid: Int,
    open val uid: Int,
    open val tid: Int = 0,
    open val content: String,
    val type: Type,
    open val timestamp: Long,
    open val uuid: String,
    open val sendState: Int
) : Replyable {
    sealed class Type(private val text: String) {
        object Text : Type("text")
        object Image : Type("image")
        object Graphics : Type("graphics")
        object ContactRequest : Type("contact-request")

        object Unknown : Type("")

        override fun toString(): String = text

        @Suppress("unused")
        object Converter {
            @TypeConverter
            fun decode(value: String?): Type {
                return when (value?.lowercase()?.trim()) {
                    "text" -> Text
                    "image" -> Image
                    "graphics" -> Graphics
                    "contact-request" -> ContactRequest
                    else -> Unknown
                }
            }

            @TypeConverter
            fun encode(type: Type): String {
                return type.toString()
            }
        }
    }

    override fun reply(): Int? = when (this) {
        is TextMessage -> reply
        is ImageMessage -> reply
        is GraphicsMessage -> reply
        else -> null
    }

    companion object {
        const val STATE_PENDING = 0
        const val STATE_SEND = 1
        const val STATE_FAILED = 2
        const val STATE_ANOTHER = 3
    }

    fun toReadable(): Message = when (this) {
        is TextMessage, is ImageMessage, is GraphicsMessage, is ContactRequest -> this
        else -> when (type) {
            Type.Text -> {
                val textContent = TextContent.decode(content)
                TextMessage(
                    id = id,
                    cid = cid,
                    uid = uid,
                    text = textContent.text,
                    reply = textContent.reply,
                    timestamp = timestamp,
                    uuid = uuid,
                    sendState = sendState
                )
            }

            Type.Image -> {
                val imageContent = ImageContent.decode(content)
                ImageMessage(
                    id = id,
                    cid = cid,
                    uid = uid,
                    url = imageContent.url,
                    reply = imageContent.reply,
                    width = imageContent.width,
                    height = imageContent.height,
                    timestamp = timestamp,
                    uuid = uuid,
                    sendState = sendState
                )
            }

            Type.Graphics -> {
                val graphicsContent = GraphicsContent.decode(content)
                GraphicsMessage(
                    id = id,
                    cid = cid,
                    uid = uid,
                    text = graphicsContent.text,
                    url = graphicsContent.url,
                    reply = graphicsContent.reply,
                    width = graphicsContent.width,
                    height = graphicsContent.height,
                    timestamp = timestamp,
                    uuid = uuid,
                    sendState = sendState
                )
            }

            Type.ContactRequest -> {
                ContactRequest(
                    id = id,
                    cid = cid,
                    uid = uid,
                    tid = tid,
                    text = content,
                    timestamp = timestamp,
                    uuid = uuid,
                    sendState = sendState
                )
            }
            else -> this
        }
    }
}

interface Replyable {
    fun reply(): Int?
}

data class TextMessage(
    override val id: Int,
    override val cid: Int,
    override val uid: Int,
    val text: String,
    val reply: Int?,
    override val timestamp: Long,
    override val uuid: String,
    override val sendState: Int
) : Message(
    id = id,
    cid = cid,
    uid = uid,
    tid = 0,
    content = TextContent(text, reply).let(json::encodeToString),
    type = Type.Text,
    timestamp = timestamp,
    uuid = uuid,
    sendState = sendState
)

data class ImageMessage(
    override val id: Int,
    override val cid: Int,
    override val uid: Int,
    val url: String,
    val reply: Int?,
    val width: Int = -1,
    val height: Int = -1,
    override val timestamp: Long,
    override val uuid: String,
    override val sendState: Int
) : Message(
    id = id,
    cid = cid,
    uid = uid,
    tid = 0,
    content = ImageContent(url, reply, width, height).let(json::encodeToString),
    type = Type.Image,
    timestamp = timestamp,
    uuid = uuid,
    sendState = sendState
)

data class GraphicsMessage(
    override val id: Int,
    override val cid: Int,
    override val uid: Int,
    val text: String,
    val url: String,
    val reply: Int?,
    val width: Int = -1,
    val height: Int = -1,
    override val timestamp: Long,
    override val uuid: String,
    override val sendState: Int
) : Message(
    id = id,
    cid = cid,
    uid = uid,
    tid = 0,
    content = GraphicsContent(text, url, reply, width, height).let(json::encodeToString),
    type = Type.Image,
    timestamp = timestamp,
    uuid = uuid,
    sendState = sendState
)

data class ContactRequest(
    override val id: Int,
    override val cid: Int,
    override val uid: Int,
    override val tid: Int,
    val text: String,
    override val timestamp: Long,
    override val uuid: String,
    override val sendState: Int
) : Message(
    id = id,
    cid = cid,
    uid = uid,
    tid = tid,
    content = text,
    type = Type.ContactRequest,
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

    fun toMessage() = when (Message.Type.Converter.decode(type)) {
        Message.Type.Text -> {
            val textContent = TextContent.decode(content)
            TextMessage(
                id = id,
                cid = cid,
                uid = uid,
                text = textContent.text,
                reply = textContent.reply,
                timestamp = timestamp,
                uuid = uuid,
                sendState = sendState
            )
        }

        Message.Type.Image -> {
            val imageContent = ImageContent.decode(content)
            ImageMessage(
                id = id,
                cid = cid,
                uid = uid,
                url = imageContent.url,
                reply = imageContent.reply,
                width = imageContent.width,
                height = imageContent.height,
                timestamp = timestamp,
                uuid = uuid,
                sendState = sendState
            )
        }

        Message.Type.Graphics -> {
            val graphicsContent = GraphicsContent.decode(content)
            GraphicsMessage(
                id = id,
                cid = cid,
                uid = uid,
                text = graphicsContent.text,
                url = graphicsContent.url,
                reply = graphicsContent.reply,
                width = graphicsContent.width,
                height = graphicsContent.height,
                timestamp = timestamp,
                uuid = uuid,
                sendState = sendState
            )
        }

        Message.Type.ContactRequest -> {
            ContactRequest(
                id = id,
                cid = cid,
                uid = uid,
                tid = tid,
                text = content,
                timestamp = timestamp,
                uuid = uuid,
                sendState = sendState
            )
        }

        else -> Message(
            id = id,
            cid = cid,
            uid = uid,
            tid = 0,
            content = content,
            type = Message.Type.Converter.decode(type),
            timestamp = timestamp,
            uuid = uuid,
            sendState = sendState
        )
    }
}

@Serializable
data class TextContent(
    @SerialName("text")
    val text: String,
    @SerialName("reply")
    val reply: Int?
) {
    companion object {
        fun decode(s: String): TextContent = runCatching {
            json.decodeFromString<TextContent>(s)
        }.getOrElse { TextContent("", null) }
    }
}


@Serializable
data class ImageContent(
    @SerialName("url")
    val url: String,
    @SerialName("reply")
    val reply: Int?,
    @SerialName("width")
    val width: Int = -1,
    @SerialName("height")
    val height: Int = -1
) {
    companion object {
        fun decode(s: String): ImageContent = runCatching {
            json.decodeFromString<ImageContent>(s)
        }.getOrElse {
            ImageContent("", null)
        }
    }
}


@Serializable
data class GraphicsContent(
    @SerialName("text")
    val text: String,
    @SerialName("url")
    val url: String,
    @SerialName("reply")
    val reply: Int?,
    @SerialName("width")
    val width: Int = -1,
    @SerialName("height")
    val height: Int = -1
) {
    companion object {
        fun decode(s: String): GraphicsContent = runCatching {
            json.decodeFromString<GraphicsContent>(s)
        }.getOrElse { GraphicsContent("", "", null) }
    }
}

