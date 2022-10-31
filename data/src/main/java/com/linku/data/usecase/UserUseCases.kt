package com.linku.data.usecase

import com.linku.domain.Strategy
import com.linku.domain.auth.Authenticator
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.User
import com.linku.domain.repository.ConversationRepository
import com.linku.domain.repository.UserRepository
import com.linku.domain.room.dao.ConversationDao
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
        strategy: Strategy = Strategy.NetworkElseCache
    ): User? = try {
        repository.findById(id, strategy)
    } catch (e: Exception) {
        e.printStackTrace()
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