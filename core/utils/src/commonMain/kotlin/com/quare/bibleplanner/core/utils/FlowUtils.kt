package com.quare.bibleplanner.core.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlin.time.Duration

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

/**
 * Emits the first value immediately and then at most one value per [window], always the latest one
 * produced meanwhile.
 *
 * Unlike [kotlinx.coroutines.flow.sample], the first value is not withheld for a whole window, so a
 * throttled stream still paints its initial state right away.
 *
 * @param window The minimum interval between two emissions.
 */
fun <T> Flow<T>.throttleLatest(window: Duration): Flow<T> = conflate().transform { value ->
    emit(value)
    delay(window)
}
