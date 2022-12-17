package com.linku.im.screen

import androidx.annotation.CallSuper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.linku.im.vm

abstract class BaseViewModel<S, E>(private val emptyState: S) : ViewModel() {

    private var _state = mutableStateOf(emptyState)

    protected var writable by _state
    val readable: S by _state

    internal abstract fun onEvent(event: E)

    protected open fun onMessage(message: String?) {
        vm.onMessage(message)
    }

    @CallSuper
    open fun restore() {
        writable = emptyState
    }
}
