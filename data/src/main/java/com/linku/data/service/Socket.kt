package com.linku.data.service

import kotlinx.serialization.Serializable

@Serializable
data class Socket<T>(
    val data: T,
    val type: String
)