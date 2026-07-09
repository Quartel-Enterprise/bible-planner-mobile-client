package com.quare.bibleplanner.core.model

import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class NavigationEventBus {
    private val _events: MutableSharedFlow<NavKey> = MutableSharedFlow(replay = 1)
    val events: SharedFlow<NavKey> = _events.asSharedFlow()

    fun send(route: NavKey) {
        _events.tryEmit(route)
    }

    fun reset() {
        _events.resetReplayCache()
    }
}
