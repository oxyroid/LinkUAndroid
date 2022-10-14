package com.linku.domain.repository

import android.net.Uri
import com.linku.domain.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun signIn(
        email: String,
        password: String,
        behaviour: AfterSignInBehaviour
    ): Flow<SignInState>

    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        realName: String?
    ): Result<Unit>

    suspend fun signOut()

    suspend fun verifyEmailCode(code: String): Result<Unit>

    suspend fun verifyEmail(): Result<Unit>

    fun uploadAvatar(uri: Uri): Flow<Resource<Unit>>

    sealed class SignInState {
        object Start : SignInState()
        object Syncing : SignInState()
        object Completed : SignInState()
        data class Failed(val message: String?) : SignInState()
    }

    sealed class AfterSignInBehaviour {
        data class SyncUnreadMessages(
            val duration: Long = 1000L * 60 * 60 * 24 * 3
        ) : AfterSignInBehaviour()

        object DoNothing : AfterSignInBehaviour()
    }
}
