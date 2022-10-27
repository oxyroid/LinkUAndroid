package com.linku.data.usecase

import android.net.Uri
import com.linku.core.wrapper.Resource
import com.linku.domain.auth.Authenticator
import com.linku.domain.repository.AuthRepository
import com.linku.domain.repository.AuthRepository.AfterSignInBehaviour
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
        password: String,
        behaviour: AfterSignInBehaviour = AfterSignInBehaviour.DoNothing
    ): Flow<AuthRepository.SignInState> = repository.signIn(email, password, behaviour)
}

data class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        name: String,
        realName: String? = null
    ): Result<Unit> = repository.signUp(email, password, name, realName)
}

data class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val authenticator: Authenticator
) {
    suspend operator fun invoke() {
        authenticator.update()
        repository.signOut()
    }
}

data class VerifiedEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.verifyEmail()
}

data class VerifiedEmailCodeUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(code: String): Result<Unit> = repository.verifyEmailCode(code)
}

data class UploadAvatarUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(uri: Uri): Flow<Resource<Unit>> = repository.uploadAvatar(uri)
}
