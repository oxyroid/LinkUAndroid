package com.linku.domain.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
data class User(
    @PrimaryKey
    val id: Int,
    val name: String,
    val email: String,
    val verified: Boolean,
    val realName: String?
)

@Serializable
@Keep
data class UserDTO(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String = "",
    @SerialName("email")
    val email: String = "",
    @SerialName("password")
    val password: String = "",
    @SerialName("salt")
    val salt: String = "",
    @SerialName("role")
    val role: String = "",
    @SerialName("is_verified")
    val verified: Boolean,
    @SerialName("realName")
    val realName: String? = null
) {
    fun toUser() = User(
        id = id,
        name = name,
        email = email,
        verified = verified,
        realName = realName
    )
}