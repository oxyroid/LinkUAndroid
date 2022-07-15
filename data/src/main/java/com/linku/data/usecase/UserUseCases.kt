package com.linku.data.usecase

import com.linku.domain.Resource
import com.linku.domain.emitResource
import com.linku.domain.entity.User
import com.linku.domain.repository.UserRepository
import com.linku.domain.resourceFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class UserUseCases @Inject constructor(
    val findUserUseCase: FindUserUseCase
)

data class FindUserUseCase(
    private val repository: UserRepository
) {
    operator fun invoke(id: Int): Flow<Resource<User>> = resourceFlow {
        repository.getById(id)
            .handle(::emitResource)
            .failure(::emitResource)
    }
}