package com.linku.domain.repository

import com.linku.domain.Strategy
import com.linku.domain.entity.User

interface UserRepository {
    suspend fun getById(id: Int, strategy: Strategy): User?
    suspend fun query(name: String?, email: String?): List<User>
}