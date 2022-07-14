package com.linku.domain.room.converter

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

open class ListConverter<T> {
    @TypeConverter
    fun decode(value: String): List<T> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun encode(list: List<T>): String {
        return Json.encodeToString(list)
    }
}

class StringListConverter : ListConverter<String>()
class IntListConverter : ListConverter<Int>()