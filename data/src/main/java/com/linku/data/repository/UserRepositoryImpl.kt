package com.linku.data.repository

import com.linku.domain.repository.UserRepository
import com.linku.domain.room.dao.UserDao
import com.linku.domain.sandbox
import com.linku.domain.service.UserService

class UserRepositoryImpl(
    private val userService: UserService,
    private val userDao: UserDao
) : UserRepository {

    override suspend fun getById(id: Int) = sandbox {
        userService.getById(id)
    }
}