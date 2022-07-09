package com.linku.domain

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Result<out T>(
    private val data: T? = null,
    private val code: String = "00000",
    @SerialName("msg")
    private val message: String? = null
) {
    private val isSuccess = code == "00000"

    fun peek() = data!!
    fun peekOrNull() = data

    suspend fun handle(block: suspend (T) -> Unit): Result<T> {
        if (isSuccess && data != null) block.invoke(data)
        return this
    }

    suspend fun handleUnit(block: suspend (Unit) -> Unit): Result<T> {
        if (isSuccess) block.invoke(Unit)
        return this
    }

    suspend fun catch(block: suspend (String, String) -> Unit): Result<T> {
        if (!isSuccess && message != null) block.invoke(message, code)
        return this
    }

    fun <R> map(converter: (T) -> R): Result<R> {
        if (data == null) return Result(
            code = code,
            message = message
        )
        return Result(
            data = converter(data),
            code = code,
            message = message
        )
    }

    fun toResource(defMessage: String = "Unknown Error"): Resource<T> =
        if (isSuccess) Resource.Success(data!!)
        else Resource.Failure(message ?: defMessage, code)
}

inline fun <T> sandbox(block: () -> Result<T>): Result<T> {
    return try {
        block.invoke()
    } catch (e: Exception) {
        e.printStackTrace()
        Result(
            code = "9999",
            message = e.message ?: "sandbox exception"
        )
    }
}