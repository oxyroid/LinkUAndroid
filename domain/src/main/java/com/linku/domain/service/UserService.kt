package com.linku.domain.service

import androidx.annotation.Keep
import com.linku.domain.Result
import com.linku.domain.entity.UserDTO
import io.ktor.http.*

@Keep
interface UserService {
    suspend fun getById(id: Int): Result<UserDTO>

    sealed class EndPoints(
        override val method: HttpMethod,
        override val params: Map<String, String?> = emptyMap(),
        override vararg val path: String = emptyArray()
    ) : HttpEndPoints(method, params, *path) {
        data class GetById(val id: Int) : EndPoints(
            method = HttpMethod.Get,
            path = arrayOf("users", id.toString()),
        )
    }
}