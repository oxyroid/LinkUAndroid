package com.wzk.domain.repository.user

import com.wzk.domain.LocalSharedPreference
import com.wzk.domain.entity.User
import com.wzk.domain.service.UserService
import com.wzk.domain.room.dao.UserDao
import com.wzk.wrapper.Result
import com.wzk.wrapper.sandbox

class UserRepositoryImpl(
    private val userService: UserService,
    private val userDao: UserDao,
    private val sharedPreference: LocalSharedPreference
) : UserRepository {
    override suspend fun getById(id: Int): Result<User> {
        return sandbox {
            userService.getById(id).handle(userDao::insert)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return sandbox {
            userService.login(email, password).handle {
                userDao.insert(it)
                sharedPreference.setLocalUser(it)
            }
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        username: String
    ): Result<User> {
        return sandbox {
            userService.register(email, password, username)
        }
    }
}