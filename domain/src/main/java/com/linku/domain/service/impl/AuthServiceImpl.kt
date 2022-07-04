package com.linku.domain.service.impl

import com.linku.domain.entity.UserDTO
import com.linku.domain.service.AuthService
import com.linku.domain.Result
import com.linku.domain.sandbox
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class AuthServiceImpl(
    private val client: HttpClient
) : AuthService {

    override suspend fun register(
        email: String,
        password: String,
        nickName: String,
        realName: String?
    ): Result<Unit> = sandbox {
        client.post {
            url(AuthService.EndPoints.Register(email, password, nickName, realName).url)
        }.body()
    }

    override suspend fun login(email: String, password: String): Result<UserDTO> = sandbox {
        client.post {
            url(AuthService.EndPoints.Login(email, password).url)
        }.body()
    }

    override suspend fun verifyEmail(code: Int): Result<Unit> = sandbox {
        client.get {
            url(AuthService.EndPoints.VerifyEmail(code).url)
        }.body()
    }

    override suspend fun resendEmail(): Result<Unit> = sandbox {
        client.get { url(AuthService.EndPoints.ResendEmail.url) }.body()
    }
}