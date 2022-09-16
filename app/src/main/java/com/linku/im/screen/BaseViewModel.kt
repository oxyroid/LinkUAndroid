package com.linku.im.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.linku.domain.Event
import com.linku.domain.eventOf

/**
 * This is a ViewModel based on the MVI architecture, which standardizes the state and event data flow
 * @param S The State data class
 * @param E The Event sealed class
 * @param emptyState The default state
 */
abstract class BaseViewModel<S, E>(emptyState: S) : ViewModel() {
    /**
     * Observe this to update UI
     */
    private var _state = mutableStateOf(
//        Proxy.newProxyInstance(S::class.javaClass.classLoader)
        emptyState
    )
    protected var writable by _state
    val readable: S by _state

    private var _message = mutableStateOf<Event<String>>(Event.Handled())
    var message by _message
        private set


    /**
     * This function is used to submit intent and hand it over to the use case
     *
     * Finally distribute the result to state and event
     *
     * @param event The intent saved as event
     */
    abstract fun onEvent(event: E)

    protected fun onMessage(message: String?) {
        this.message = eventOf(message ?: return)
    }
}
