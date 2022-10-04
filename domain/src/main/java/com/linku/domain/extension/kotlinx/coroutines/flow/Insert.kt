package com.linku.domain.extension.kotlinx.coroutines.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

inline fun <E> Flow<E>.insert(crossinline block: (E?, E) -> E?): Flow<E?> {
    var pre: E? = null
    return transform { new ->
        pre = pre.let { e ->
            emit(e)
            block(e, new).also { emit(it) }
            emit(new)
            new
        } ?: new
    }
}

inline fun <E> Flow<E>.insertNotNull(crossinline block: (E, E) -> E?): Flow<E> {
    var pre: E? = null
    return transform { new ->
        pre = pre?.let { nonnull ->
            emit(nonnull)
            block(nonnull, new)?.also { emit(it) }
            emit(new)
            new
        } ?: new
    }
}
