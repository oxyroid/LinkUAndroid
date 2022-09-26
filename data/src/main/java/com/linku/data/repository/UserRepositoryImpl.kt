package com.linku.data.repository

import com.linku.domain.Strategy
import com.linku.domain.entity.User
import com.linku.domain.repository.UserRepository
import com.linku.domain.room.dao.UserDao
import com.linku.domain.service.UserService
import com.linku.domain.toResult
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userService: UserService
) : UserRepository {
    private val memory = mutableMapOf<Int, User?>()
    override suspend fun findById(id: Int, strategy: Strategy): User? = when (strategy) {
        Strategy.OnlyCache -> userDao.getById(id)
        Strategy.OnlyNetwork -> try {
            userService.getById(id).toResult()
        } catch (e: Exception) {
            Result.failure(e)
        }
            .map { it.toUser() }
            .getOrNull()

        Strategy.CacheElseNetwork -> {
            val user: User? = userDao.getById(id)
            if (user == null) {
                try {
                    userService.getById(id).toResult()
                } catch (e: Exception) {
                    Result.failure(e)
                }
                    .onSuccess {
                        userDao.insert(it.toUser())
                    }
            }
            user ?: userDao.getById(id)
        }
        Strategy.NetworkElseCache -> try {
            userService.getById(id).toResult()
        } catch (e: Exception) {
            Result.failure(e)
        }
            .onSuccess {
                userDao.insert(it.toUser())
            }
            .getOrNull()
            ?.toUser()
            ?: userDao.getById(id)
        Strategy.Memory -> memory.getOrPut(id) {
            val user: User? = userDao.getById(id)
            if (user == null) {
                try {
                    userService.getById(id).toResult()
                } catch (e: Exception) {
                    Result.failure(e)
                }
                    .onSuccess {
                        userDao.insert(it.toUser())
                    }
            }
            user ?: userDao.getById(id)
        }
    }

    override suspend fun query(name: String?, email: String?): List<User> {
        return userService.query(name, email)
            .toResult()
            .onSuccess { users ->
                users.forEach {
                    val cache = userDao.getById(it.id)
                    if (cache == null) {
                        userDao.insert(it.toUser())
                    }
                }
            }
            .getOrNull()
            ?.map { it.toUser() }
            ?: emptyList()
    }
}