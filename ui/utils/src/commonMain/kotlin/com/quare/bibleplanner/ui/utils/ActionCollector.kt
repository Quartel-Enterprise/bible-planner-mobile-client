package com.quare.bibleplanner.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun <T> ActionCollector(
    flow: Flow<T>,
    emit: suspend (T) -> Unit,
) {
    LaunchedEffect(key1 = Unit) {
        flow.collectLatest {
            emit(it)
        }
    }
}
