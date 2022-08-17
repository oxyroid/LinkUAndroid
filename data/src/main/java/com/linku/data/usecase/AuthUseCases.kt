package com.linku.data.usecase

import android.net.Uri
import com.linku.domain.Authenticator
import com.linku.domain.Resource
import com.linku.domain.emitResource
import com.linku.domain.repository.AuthRepository
import com.linku.domain.resourceFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class AuthUseCases @Inject constructor(
    val signIn: SignInUseCase,
    val signUp: SignUpUseCase,
    val signOut: SignOutUseCase,
    val verifiedEmail: VerifiedEmailUseCase,
    val verifiedEmailCode: VerifiedEmailCodeUseCase,
    val uploadAvatar: UploadAvatarUseCase,
)

data class SignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(
        email: String,
        password: String
    ): Flow<Resource<Float>> = repository.signIn(email, password)
}

data class SignUpUseCase @Inject constructor(
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

data class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val authenticator: Authenticator
) {
    operator fun invoke(): Flow<Resource<Unit>> = resourceFlow {
        authenticator.update()
        repository.signOut()
        emitResource(Unit)
    }
}

data class VerifiedEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<Resource<Unit>> = resourceFlow {
        repository.verifyEmail()
            .handleUnit(::emitResource)
            .catch(::emitResource)
    }
}

data class VerifiedEmailCodeUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(code: String): Flow<Resource<Unit>> = resourceFlow {
        repository.verifyEmailCode(code)
            .handleUnit(::emitResource)
            .catch(::emitResource)
    }
}

data class UploadAvatarUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(uri: Uri): Flow<Resource<Unit>> = repository.uploadAvatar(uri)
}