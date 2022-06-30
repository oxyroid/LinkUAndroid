package com.linku.domain.service.impl

import com.linku.domain.entity.User
import com.linku.domain.service.UserService
import com.linku.wrapper.Result
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class UserServiceImpl(
    private val client: HttpClient
) : UserService {
    override suspend fun getById(id: Int): Result<User> {
        return try {
            client.get {
                url(UserService.EndPoints.GetById(id).url)
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            Result(code = Result.Directory.UNKNOWN_ERROR.code)
        }
    }

    override suspend fun register(email: String, password: String): Result<User> {
        return try {
            client.get {
                url(UserService.EndPoints.Register(email, password).url)
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            Result(code = Result.Directory.UNKNOWN_ERROR.code)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            client.get {
                url(UserService.EndPoints.Login(email, password).url)
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            Result(code = Result.Directory.UNKNOWN_ERROR.code)
        }
    }
}