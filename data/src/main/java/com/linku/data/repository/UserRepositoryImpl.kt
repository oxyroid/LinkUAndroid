package com.linku.data.repository

import com.linku.domain.Strategy
import com.linku.domain.entity.User
import com.linku.domain.repository.UserRepository
import com.linku.domain.room.dao.UserDao
import com.linku.domain.service.UserService

class UserRepositoryImpl(
    private val userService: UserService,
    private val userDao: UserDao
) : UserRepository {
    private val memory = mutableMapOf<Int, User?>()
    override suspend fun getById(id: Int, strategy: Strategy): User? = when (strategy) {
        Strategy.OnlyCache -> userDao.getById(id)
        Strategy.OnlyNetwork -> userService.getById(id).map { it.toUser() }.peekOrNull()
        Strategy.CacheElseNetwork -> {
            val user: User? = userDao.getById(id)
            if (user == null) {
                userService.getById(id)
                    .handle {
                        userDao.insert(it.toUser())
                    }
            }
            user ?: userDao.getById(id)
        }
        Strategy.NetworkThenCache -> {
            userService.getById(id)
                .handle {
                    userDao.insert(it.toUser())
                }
            userDao.getById(id)
        }
        Strategy.Memory -> memory.getOrPut(id) {
            val user: User? = userDao.getById(id)
            if (user == null) {
                userService.getById(id)
                    .handle {
                        userDao.insert(it.toUser())
                    }
            }
            user ?: userDao.getById(id)
        }
    }
}