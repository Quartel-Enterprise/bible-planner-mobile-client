package com.quare.bibleplanner.core.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppForegroundStateHolder {
    val isForeground: StateFlow<Boolean>
        field = MutableStateFlow<Boolean>(false)

    fun onForegrounded() {
        isForeground.value = true
    }

    fun onBackgrounded() {
        isForeground.value = false
    }
}
