package com.linku.im.screen

import androidx.annotation.CallSuper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.linku.core.wrapper.Event
import com.linku.core.wrapper.eventOf

abstract class BaseViewModel<S, E>(private val emptyState: S) : ViewModel() {

    private var _state = mutableStateOf(emptyState)

    protected var writable by _state
    val readable: S by _state

    private var _message = mutableStateOf<Event<String>>(Event.Handled())
    var message by _message
        private set

    internal abstract fun onEvent(event: E)

    protected fun onMessage(message: String?) {
        this.message = eventOf(message ?: return)
    }

    @CallSuper
    open fun restore() {
        writable = emptyState
    }
}
