package com.thxbrop.data2.repository

import com.linku.domain.Strategy
import com.linku.domain.entity.User
import com.linku.domain.repository.UserRepository
import com.linku.domain.result
import com.linku.domain.room.dao.UserDao
import com.linku.domain.service.UserService
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userService: UserService
) : UserRepository {
    private val memory = mutableMapOf<Int, User?>()
    override suspend fun getById(id: Int, strategy: Strategy): User? = when (strategy) {
        Strategy.OnlyCache -> userDao.getById(id)
        Strategy.OnlyNetwork -> result { userService.getById(id) }
            .map { it.toUser() }.getOrNull()

        Strategy.CacheElseNetwork -> {
            val user: User? = userDao.getById(id)
            if (user == null) {
                result { userService.getById(id) }
                    .onSuccess {
                        userDao.insert(it.toUser())
                    }
            }
            user ?: userDao.getById(id)
        }
        Strategy.NetworkThenCache -> {
            result { userService.getById(id) }
                .onSuccess {
                    userDao.insert(it.toUser())
                }
            userDao.getById(id)
        }
        Strategy.Memory -> memory.getOrPut(id) {
            val user: User? = userDao.getById(id)
            if (user == null) {
                result { userService.getById(id) }
                    .onSuccess {
                        userDao.insert(it.toUser())
                    }
            }
            user ?: userDao.getById(id)
        }
    }
}