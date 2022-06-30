package com.wzk.domain.repository.user

import com.wzk.domain.entity.User
import com.wzk.wrapper.Result

interface UserRepository {
    suspend fun getById(id: Int): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String): Result<User>
}