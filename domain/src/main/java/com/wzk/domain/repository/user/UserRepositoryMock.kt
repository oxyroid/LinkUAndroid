package com.wzk.domain.repository.user

import com.wzk.domain.LocalSharedPreference
import com.wzk.domain.entity.User
import com.wzk.wrapper.Result

class UserRepositoryMock(
    private val localSharedPreference: LocalSharedPreference
) : UserRepository {
    private val users = mutableMapOf<User, String>()
    override suspend fun getById(id: Int): Result<User> {
        return users.keys
            .find { it.id == id }
            ?.let(::Result)
            ?: Result(code = 999)
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return users.entries
            .find { it.key.email == email && it.value == password }
            ?.key
            ?.let {
                localSharedPreference.setLocalUser(it)
                Result(it)
            }
            ?: Result(code = 999)
    }

    override suspend fun register(
        email: String,
        password: String,
        username: String
    ): Result<User> {
        val containEmail = users.keys.find { it.email == email } != null
        if (containEmail) {
            return Result(code = 999)
        }
        val user = User(
            id = users.size + 1,
            username,
            email
        )
        users[user] = password
        return Result(user)
    }
}