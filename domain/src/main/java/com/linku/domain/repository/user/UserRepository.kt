package com.linku.domain.repository.user

import com.linku.domain.entity.User
import com.linku.wrapper.Result

interface UserRepository {
    suspend fun getById(id: Int): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String): Result<User>
}