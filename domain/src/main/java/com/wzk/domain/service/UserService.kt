package com.wzk.domain.service

import com.wzk.domain.entity.User
import com.wzk.wrapper.Result

interface UserService {
    suspend fun getById(id: Int): Result<User>

    suspend fun register(
        email: String,
        password: String
    ): Result<User>

    suspend fun login(
        email: String,
        password: String
    ): Result<User>

    companion object {
        private const val BASE_URL = "http://im.rexue.work/auth"
    }

    sealed class EndPoints(val url: String) {
        data class GetById(val id: Int) : EndPoints("$BASE_URL/$id")
        data class Register(
            val email: String,
            val password: String
        ) : EndPoints("$BASE_URL/register?email=$email&password=$password")

        data class Login(
            val email: String,
            val password: String
        ) : EndPoints("$BASE_URL/login?email=$email&password=$password")
    }

}

