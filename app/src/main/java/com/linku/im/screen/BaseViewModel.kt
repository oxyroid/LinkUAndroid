package com.linku.im.screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

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
    protected val _state = mutableStateOf(emptyState)
    val state: State<S> = _state

    /**
     * This function is used to submit intent and hand it over to the use case
     *
     * Finally distribute the result to state and event
     *
     * @param event The intent saved as event
     */
    abstract fun onEvent(event: E)
}