package com.linku.data.usecase

import com.linku.domain.Auth
import com.linku.domain.Resource
import com.linku.domain.emitResource
import com.linku.domain.entity.User
import com.linku.domain.repository.AuthRepository
import com.linku.domain.resourceFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class AuthUseCases @Inject constructor(
    val loginUseCase: LoginUseCase,
    val registerUseCase: RegisterUseCase,
    val logoutUseCase: LogoutUseCase
)

data class LoginUseCase(
    private val repository: AuthRepository
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
    private val repository: AuthRepository
) {
    operator fun invoke(
        email: String,
        password: String,
        nickName: String,
        realName: String? = null
    ): Flow<Resource<Unit>> = resourceFlow {
        repository.register(email, password, nickName, realName)
            .handleUnit(::emitResource)
            .catch(::emitResource)
    }
}

object LogoutUseCase {
    operator fun invoke(): Flow<Resource<Unit>> = resourceFlow {
        Auth.update()
        emitResource(Unit)
    }
}

