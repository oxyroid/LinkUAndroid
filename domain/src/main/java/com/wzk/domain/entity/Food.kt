package com.wzk.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.wzk.domain.room.converter.ListConverter

@Entity
@TypeConverters(ListConverter::class)
data class Food(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String?,
    val price: Float,
    val img: String
)
