package com.linku.domain.room.converter

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ListConverter {
    @TypeConverter
    fun decode(value: String): List<String> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun encode(list: List<String>): String {
        return Json.encodeToString(list)
    }
}