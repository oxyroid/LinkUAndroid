package com.linku.data.usecase

import com.linku.domain.Strategy
import com.linku.domain.entity.User
import com.linku.domain.repository.UserRepository
import javax.inject.Inject

data class UserUseCases @Inject constructor(
    val findUser: FindUserUseCase
)

data class FindUserUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        id: Int,
        strategy: Strategy = Strategy.NetworkThenCache
    ): User? = repository.getById(id, strategy)

}