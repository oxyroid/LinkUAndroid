package com.linku.domain

import kotlinx.serialization.*
import kotlin.Result

@Serializable
data class BackendResult<out T>(
    @SerialName("data")
    val data: T? = null,
    @SerialName("code")
    val code: String = "00000",
    @SerialName("msg")
    val message: String? = null
) {
    @Suppress("UNCHECKED_CAST")
    val dataOrUnit:T get() = (data ?: Unit) as T
}

data class BackendException(override val message: String?) : RuntimeException(message)

inline fun <reified T> result(catcher: () -> BackendResult<T>): Result<T> = try {
    catcher().let {
        if (it.code == "00000") Result.success(it.dataOrUnit)
        else Result.failure(BackendException(it.message))
    }
} catch (e: Exception) {
    Result.failure(BackendException(e.message))
}

inline fun <reified T> BackendResult<T>.toResult(): Result<T> = result { this }