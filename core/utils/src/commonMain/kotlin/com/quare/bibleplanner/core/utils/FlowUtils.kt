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

fun <T> Flow<T>.throttleLatest(window: Duration): Flow<T> = conflate().transform { value ->
    emit(value)
    delay(window)
}
