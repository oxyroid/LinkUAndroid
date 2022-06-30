package com.linku.domain.usecase

import com.linku.domain.entity.User
import com.linku.domain.repository.user.UserRepository
import com.linku.wrapper.Resource
import com.linku.wrapper.emitResource
import com.linku.wrapper.resourceFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class UserUseCases @Inject constructor(
    val loginUseCase: LoginUseCase,
    val registerUseCase: RegisterUseCase,
    val findUserUseCase: FindUserUseCase
)

data class LoginUseCase(
    private val repository: UserRepository
) {
    operator fun invoke(
        email: String,
        password: String
    ): Flow<Resource<User>> = resourceFlow {
        repository.login(email, password)
            .handle(::emitResource)
            .catch(::emitResource)
    }
}

data class RegisterUseCase(
    private val repository: UserRepository
) {
    operator fun invoke(
        email: String,
        password: String
    ): Flow<Resource<User>> = resourceFlow {
        repository.register(email, password)
            .handle(::emitResource)
            .catch(::emitResource)
    }
}


class FindUserUseCase(
    private val repository: UserRepository
) {
    operator fun invoke(id: Int): Flow<Resource<User>> = resourceFlow {
        repository.getById(id)
            .handle(::emitResource)
            .catch(::emitResource)
    }
}