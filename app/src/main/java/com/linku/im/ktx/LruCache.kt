package com.linku.im.ktx

import androidx.collection.LruCache

fun <K : Any, V : Any> lruCacheOf(vararg cache: Pair<K, V>) =
    LruCache<K, V>((Runtime.getRuntime().maxMemory() / 8).toInt()).apply {
        cache.forEach { put(it.first, it.second) }
    }

fun <K : Any> LruCache<K, *>.containsKey(key: K): Boolean = containsKeyLambda { key }

fun <K : Any> LruCache<K, *>.containsKeyLambda(key: () -> K): Boolean = get(key()) != null