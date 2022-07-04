package com.linku.domain.repository.user

import com.linku.domain.Result
import com.linku.domain.entity.User

interface UserRepository {
    suspend fun getById(id: Int): Result<User>
}