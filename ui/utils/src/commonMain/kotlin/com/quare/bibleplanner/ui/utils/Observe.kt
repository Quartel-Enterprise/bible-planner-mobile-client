package com.quare.bibleplanner.ui.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun <T> ViewModel.observe(
    flow: Flow<T>,
    collector: suspend (T) -> Unit,
) {
    flow.onEach(collector).launchIn(viewModelScope)
}
