package com.quare.bibleplanner.core.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppForegroundStateHolder {
    private val _isForeground: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isForeground: StateFlow<Boolean> = _isForeground.asStateFlow()

    fun onForegrounded() {
        _isForeground.value = true
    }

    fun onBackgrounded() {
        _isForeground.value = false
    }
}
