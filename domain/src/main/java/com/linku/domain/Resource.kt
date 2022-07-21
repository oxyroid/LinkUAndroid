package com.linku.domain

import androidx.annotation.Keep
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

/**
 * A data wrapper
 *
 * Data request state wrapper, usually used in a flow collector
 */
sealed class Resource<out T> {
    @Serializable
    @Keep
    object Loading : Resource<Nothing>()

    @Serializable
    @Keep
    data class Success<T>(val data: T) : Resource<T>()

    @Serializable
    @Keep
    data class Failure<T>(
        val message: String,
        val code: String = "?"
    ) : Resource<T>()

    fun toUnit(): Resource<Unit> = when (this) {
        Loading -> Loading
        is Success -> Success(Unit)
        is Failure -> Failure(message, code)
    }
}

suspend inline fun <T> FlowCollector<Resource<T>>.emitResource(data: T) =
    emit(Resource.Success(data))

suspend inline fun <T> FlowCollector<Resource<T>>.emitResource(
    message: String = "Unknown Error",
    code: String = "?"
) = emit(Resource.Failure(message, code))

fun <T> resourceFlow(flowCollector: suspend FlowCollector<Resource<T>>.() -> Unit) = flow {
    emit(Resource.Loading)
    flowCollector.invoke(this)
}