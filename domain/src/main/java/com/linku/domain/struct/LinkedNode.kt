package com.linku.domain.struct

import androidx.compose.runtime.Stable

@Stable
data class LinkedNode<E>(
    val value: E,
    val next: LinkedNode<E>? = null
)

val <E> LinkedNode<E>.hasNext: Boolean get() = next != null
