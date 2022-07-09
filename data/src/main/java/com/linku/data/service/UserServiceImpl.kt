package com.linku.data.service

import com.linku.domain.BuildConfig
import com.linku.domain.Result
import com.linku.domain.entity.UserDTO
import com.linku.domain.sandbox
import com.linku.domain.service.UserService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class UserServiceImpl(
    private val client: HttpClient
) : UserService {
    private suspend inline fun <reified R> execute(endPoint: UserService.EndPoints): R {
        return client.request(BuildConfig.BASE_URL) {
            method = endPoint.method
            url {
                path(*endPoint.path)
                endPoint.params.forEach { (k, v) ->
                    v?.also { parameters.append(k, it) }
                }
            }
        }.body()
    }

    override suspend fun getById(id: Int): Result<UserDTO> = sandbox {
        val endPoint = UserService.EndPoints.GetById(id)
        execute(endPoint)
    }
}