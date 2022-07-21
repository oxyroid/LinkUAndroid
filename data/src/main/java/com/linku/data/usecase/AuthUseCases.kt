package com.linku.data.usecase

import com.linku.domain.Auth
import com.linku.domain.Resource
import com.linku.domain.emitResource
import com.linku.domain.repository.AuthRepository
import com.linku.domain.resourceFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class AuthUseCases @Inject constructor(
    val signIn: SignInUseCase,
    val signUp: SignUpUseCase,
    val logout: SignOutUseCase,
    val verifiedEmail: VerifiedEmailUseCase,
    val verifiedEmailCode: VerifiedEmailCodeUseCase
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
        name: String,
        realName: String? = null
    ): Flow<Resource<Unit>> = resourceFlow {
        repository.signUp(email, password, name, realName)
            .handleUnit(::emitResource)
            .catch(::emitResource)
    }
}

data class SignOutUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<Resource<Unit>> = resourceFlow {
        Auth.update()
        repository.signOut()
        emitResource(Unit)
    }
}

data class VerifiedEmailUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<Resource<Unit>> = resourceFlow {
        repository.verifyEmail()
            .handleUnit(::emitResource)
            .catch(::emitResource)
    }
}

data class VerifiedEmailCodeUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(code: String): Flow<Resource<Unit>> = resourceFlow {
        repository.verifyEmailCode(code)
            .handleUnit(::emitResource)
            .catch(::emitResource)
    }
}