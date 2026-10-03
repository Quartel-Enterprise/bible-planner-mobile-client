package com.quare.bibleplanner.core.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlin.time.Duration

fun <T> MutableStateFlow<T>.updateValue(value: T) {
    update { value }
}

// Why: unlike sample, the first value is not withheld for a window, so a throttled stream
// still paints its initial state right away.
fun <T> Flow<T>.throttleLatest(window: Duration): Flow<T> = conflate().transform { value ->
    emit(value)
    delay(window)
}
