package com.wzk.wrapper

data class Result<out T>(
    private val data: T? = null,
    private val code: Int = 200,
    private val message: String? = null
) {
    val isSuccess = code == 200

    fun peek() = data!!
    fun peekOrNull() = data

    suspend fun handle(block: suspend (T) -> Unit): Result<T> {
        if (isSuccess) block.invoke(data!!)
        return this
    }

    suspend fun catch(block: suspend (Int, String) -> Unit): Result<T> {
        if (!isSuccess && message != null) block.invoke(code, message)
        return this
    }
}

inline fun <T> sandbox(block: () -> Result<T>): Result<T> {
    return try {
        block.invoke()
    } catch (e: Exception) {
        e.printStackTrace()
        Result(code = 999, message = e.message ?: "sandbox exception")
    }
}