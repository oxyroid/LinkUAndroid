package com.linku.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class User(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String,
    val verified: Boolean,
    val realName: String?
)

@Serializable
data class UserDTO(
    val id: Int,
    @SerialName("nickname")
    @SerializedName("nickname")
    val name: String,
    val email: String,
    val password: String,
    val salt: String,
    val role: String,
    @SerialName("is_verified")
    @SerializedName("is_verified")
    val verified: Boolean,
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