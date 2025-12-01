package com.quare.bibleplanner.core.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Updates the value held by this [MutableStateFlow] with the provided [value].
 *
 * This is a convenience extension that delegates to [MutableStateFlow.update],
 * ensuring that the state is replaced with the new value in a concise and
 * readable way. It is particularly useful when you want to set the state
 * without referencing the lambda receiver explicitly.
 *
 * Example usage:
 * ```
 * val state = MutableStateFlow(0)
 * state.updateValue(5) // state now holds 5
 * ```
 *
 * @param value The new value to set into the [MutableStateFlow].
 */
fun <T> MutableStateFlow<T>.updateValue(value: T) {
    update { value }
}
