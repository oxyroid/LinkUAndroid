package com.linku.domain.room.converter

import androidx.room.TypeConverter
import com.linku.domain.entity.Conversation

open class ConversationTypeConverter {
    @TypeConverter
    fun decode(value: Int?): Conversation.Type {
        return Conversation.Type.parse(value ?: -1)
    }

    @TypeConverter
    fun encode(type: Conversation.Type): Int {
        return type.type
    }
}