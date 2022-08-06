package com.linku.domain.room.converter

import androidx.room.TypeConverter
import com.linku.domain.entity.Message

open class MessageTypeConverter {
    @TypeConverter
    fun decode(value: String?): Message.Type {
        return Message.Type.parse(value ?: "")
    }

    @TypeConverter
    fun encode(type: Message.Type): String {
        return type.toString()
    }
}