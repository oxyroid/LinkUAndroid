package com.linku.domain.service

import com.linku.domain.BuildConfig
import com.linku.domain.Result
import com.linku.domain.common.buildUrl
import com.linku.domain.entity.UserDTO

interface UserService {
    suspend fun getById(id: Int): Result<UserDTO>

    companion object {
        private const val BASE_URL = "${BuildConfig.BASE_URL}/users"
    }

    sealed class EndPoints(val url: String) {
        // GET
        data class GetById(val id: Int) : EndPoints(
            buildUrl(BASE_URL) {
                path(id)
            }
        )
    }
}