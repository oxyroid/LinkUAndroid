package com.wzk.wrapper

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

/**
 * A data wrapper
 *
 * Data request state wrapper, usually used in a flow collector
 */
sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Failure<T>(val code: Int, val message: String) : Resource<T>()
}

suspend inline fun <T> FlowCollector<Resource<T>>.emitResource(data: T) =
    emit(Resource.Success(data))

suspend inline fun <T> FlowCollector<Resource<T>>.emitResource(code: Int, message: String) =
    emit(Resource.Failure(code, message))

fun <T> resourceFlow(flowCollector: suspend FlowCollector<Resource<T>>.() -> Unit) = flow {
    emit(Resource.Loading)
    flowCollector.invoke(this)
}