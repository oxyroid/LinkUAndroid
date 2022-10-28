@file:Suppress("unused")

package com.linku.im.ktx.runtime

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun <T> rememberUpdatedStateWithLifecycle(
    initialState: T,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    context: CoroutineContext = EmptyCoroutineContext,
    updater: () -> T
): State<T> = produceState(initialState, minActiveState, context, updater, lifecycleOwner) {
    lifecycleOwner.repeatOnLifecycle(minActiveState) {
        when (context) {
            EmptyCoroutineContext -> this@produceState.value = updater()
            else -> withContext(context) { this@produceState.value = updater() }
        }
    }
}

@Composable
fun LifecycleEffect(block: LifecycleOwner.(Lifecycle.Event) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver(block)
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
