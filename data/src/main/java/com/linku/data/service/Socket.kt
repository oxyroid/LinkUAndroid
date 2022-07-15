package com.linku.data.service

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class Socket<T>(
    val data: T,
    val type: String
)