package com.linku.domain.service

import androidx.annotation.Keep
import com.linku.domain.Result
import com.linku.domain.entity.UserDTO
import io.ktor.http.*

@Keep
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

    sealed class EndPoints(
        override val method: HttpMethod,
        override val params: Map<String, String?> = emptyMap(),
        override vararg val path: String = emptyArray()
    ) : HttpEndPoints(method, params, *path) {
        // GET
        class VerifyEmail(
            code: Int
        ) : EndPoints(
            method = HttpMethod.Get,
            path = arrayOf(code.toString())
        )

        // GET
        object ResendEmail : EndPoints(
            method = HttpMethod.Get,
            path = arrayOf("register", "resend")
        )

        // POST
        class Register(
            email: String,
            password: String,
            nickName: String,
            realName: String?
        ) : EndPoints(
            method = HttpMethod.Post,
            path = arrayOf("auth", "register"),
            params = mapOf(
                "email" to email,
                "password" to password,
                "nickname" to nickName,
                "realName" to realName,
            )
        )

        // POST
        data class Login(
            val email: String,
            val password: String
        ) : EndPoints(
            method = HttpMethod.Post,
            path = arrayOf("auth", "login"),
            params = mapOf(
                "email" to email,
                "password" to password
            )
        )
    }
}