package com.linku.domain.service

import com.linku.domain.BuildConfig
import com.linku.domain.Result
import com.linku.domain.common.buildUrl
import com.linku.domain.entity.UserDTO

interface AuthService {

    suspend fun register(
        email: String,
        password: String,
        nickName: String,
        realName: String?
    ): Result<Unit>

    suspend fun login(
        email: String,
        password: String
    ): Result<UserDTO>

    suspend fun verifyEmail(code: Int): Result<Unit>

    suspend fun resendEmail(): Result<Unit>

    companion object {
        const val BASE_URL = "${BuildConfig.BASE_URL}/auth"
    }

    sealed class EndPoints(val url: String) {

        // GET
        data class VerifyEmail(
            val code: Int
        ) : EndPoints(
            buildUrl(BASE_URL) {
                path("register")
                path(code)
            }
        )

        // GET
        object ResendEmail : EndPoints(
            buildUrl(BASE_URL) {
                path("register")
                path("resend")
            }
        )

        // POST
        data class Register(
            val email: String,
            val password: String,
            val nickName: String,
            val realName: String?
        ) : EndPoints(
            buildUrl(BASE_URL) {
                path("register")
                query("email", email)
                query("password", password)
                query("nickName", nickName)
                query("realName", realName)
            }
        )

        // POST
        data class Login(
            val email: String,
            val password: String
        ) : EndPoints(
            buildUrl(BASE_URL) {
                path("login")
                query("email", email)
                query("password", password)
            }
        )
    }
}