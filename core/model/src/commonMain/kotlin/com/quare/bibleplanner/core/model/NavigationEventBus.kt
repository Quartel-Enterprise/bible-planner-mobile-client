package com.quare.bibleplanner.core.model

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class NavigationEventBus {
    private val _events: MutableSharedFlow<Any> = MutableSharedFlow(replay = 1)
    val events: SharedFlow<Any> = _events.asSharedFlow()

    fun send(route: Any) {
        _events.tryEmit(route)
    }

    fun reset() {
        _events.resetReplayCache()
    }
}
