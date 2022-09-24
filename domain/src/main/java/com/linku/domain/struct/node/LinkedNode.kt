package com.linku.domain.struct.node

import androidx.compose.runtime.Immutable
import java.util.Stack

/**
 * Immutable LinkedList Node.
 *
 * Null value is not allowed.
 *
 * Usually used to replace [Stack] in Compose.
 * @param value The value which it is held.
 * @param cache The another LinkedNode.
 */
@Immutable
data class LinkedNode<E : Any>(
    val value: E,
    val cache: LinkedNode<E>? = null
)

/**
 * Tests if its cache is existed.
 *
 * Returns: true if and only if its cache is not existed; false otherwise.
 */
val <E : Any> LinkedNode<E>.hasCache: Boolean get() = cache != null

/**
 * @return a new LinkedNode which its value is new one and cache is previous node.
 */
fun <E : Any> LinkedNode<E>.forward(value: E): LinkedNode<E> = LinkedNode(value, this)

/**
 * @return cache node or current node if the cache node is not existed.
 */
fun <E : Any> LinkedNode<E>.remain(): LinkedNode<E> = cache ?: this