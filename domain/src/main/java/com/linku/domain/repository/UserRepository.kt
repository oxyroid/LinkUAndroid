package com.linku.domain.repository

import com.linku.domain.Result
import com.linku.domain.entity.UserDTO

interface UserRepository {
    suspend fun getById(id: Int): Result<UserDTO>
}