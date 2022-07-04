package com.linku.domain.service.impl

import com.linku.domain.Result
import com.linku.domain.entity.UserDTO
import com.linku.domain.sandbox
import com.linku.domain.service.UserService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class UserServiceImpl(
    private val client: HttpClient
) : UserService {
    override suspend fun getById(id: Int): Result<UserDTO> = sandbox {
        client.get {
            url(UserService.EndPoints.GetById(id).url)
        }.body()
    }
}