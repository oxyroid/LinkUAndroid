package com.linku.domain.repository

import androidx.annotation.Keep
import com.linku.domain.Result
import com.linku.domain.entity.User

@Keep
interface UserRepository {
    suspend fun getById(id: Int): Result<User>
}