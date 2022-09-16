package com.linku.data.usecase

import com.linku.domain.Strategy
import com.linku.domain.entity.User
import com.linku.domain.repository.UserRepository
import javax.inject.Inject

data class UserUseCases @Inject constructor(
    val findUser: FindUserUseCase,
    val query: QueryUsersUseCase
)

data class FindUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        id: Int,
        strategy: Strategy = Strategy.NetworkThenCache
    ): User? = try {
        repository.getById(id, strategy)
    } catch (e: Exception) {
        null
    }
}

data class QueryUsersUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        name: String? = null,
        email: String? = null
    ): List<User> = try {
        repository.query(name, email)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}