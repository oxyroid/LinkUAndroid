package com.linku.data.service

import com.linku.domain.BuildConfig
import com.linku.domain.entity.UserDTO
import com.linku.domain.sandbox
import com.linku.domain.service.AuthService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AuthServiceImpl(
    private val client: HttpClient
) : AuthService {
    private suspend inline fun <reified R> execute(endPoint: AuthService.EndPoints): R {
        return client.request(BuildConfig.BASE_URL) {
            method = endPoint.method
            url {
                path(*endPoint.path)
                endPoint.params.forEach { (k, v) ->
                    v?.also { parameters.append(k, it) }
                }
            }
        }.body()
    }

    override suspend fun register(
        email: String,
        password: String,
        nickName: String,
        realName: String?
    ) = sandbox<Unit> {
        val endPoint = AuthService.EndPoints.Register(email, password, nickName, realName)
        execute(endPoint)
    }

    override suspend fun login(email: String, password: String) = sandbox<UserDTO> {
        val endPoint = AuthService.EndPoints.Login(email, password)
        execute(endPoint)
    }

    override suspend fun verifyEmail(code: Int) = sandbox<Unit> {
        val endPoint = AuthService.EndPoints.VerifyEmail(code)
        execute(endPoint)
    }

    override suspend fun resendEmail() = sandbox<Unit> {
        val endPoint = AuthService.EndPoints.ResendEmail
        execute(endPoint)
    }
}