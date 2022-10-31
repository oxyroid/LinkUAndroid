package com.linku.domain.room.converter

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class IntList {
    @TypeConverter
    fun decode(value: String?): List<Int> {
        return value?.let { Json.decodeFromString(value) } ?: emptyList()
    }

    @TypeConverter
    fun encode(list: List<Int>?): String {
        return Json.encodeToString(list ?: emptyList())
    }
}