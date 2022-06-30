package com.linku.domain.repository.user

import com.linku.domain.LocalSharedPreference
import com.linku.domain.entity.User
import com.linku.domain.room.dao.UserDao
import com.linku.domain.service.UserService
import com.linku.wrapper.Result
import com.linku.wrapper.sandbox

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
        password: String
    ): Result<User> {
        return sandbox {
            userService.register(email, password)
        }
    }
}