package com.linku.data.usecase

import com.linku.domain.Auth
import com.linku.domain.Resource
import com.linku.domain.emitResource
import com.linku.domain.repository.AuthRepository
import com.linku.domain.resourceFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class AuthUseCases @Inject constructor(
    val signInUseCase: SignInUseCase,
    val signUpUseCase: SignUpUseCase,
    val logoutUseCase: LogoutUseCase,
)

data class SignInUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(
        email: String,
        password: String
    ): Flow<Resource<Unit>> = resourceFlow {
        repository.signIn(email, password)
            .handle(::emitResource)
            .catch(::emitResource)
    }
}

data class SignUpUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(
        email: String,
        password: String,
        nickName: String,
        realName: String? = null
    ): Flow<Resource<Unit>> = resourceFlow {
        repository.signUp(email, password, nickName, realName)
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
