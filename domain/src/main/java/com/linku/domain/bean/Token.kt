package com.linku.domain.bean

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Token(
    val id: Int,
    val token: String
)
