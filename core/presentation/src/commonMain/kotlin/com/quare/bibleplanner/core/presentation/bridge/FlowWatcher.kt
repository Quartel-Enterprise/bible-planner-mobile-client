package com.quare.bibleplanner.core.presentation.bridge

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/*
 * Why: Swift and JS can't collect a Flow, so the native UIs observe a ViewModel's state and actions
 * through a callback, delivered on the main thread.
 */
class FlowWatcher<T : Any>(
    private val flow: Flow<T>,
) {
    // Why: an uncaught exception would terminate the iOS app; a failed observation is logged instead.
    private val loggingHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { "FlowWatcher stopped observing after an exception" }
    }

    fun observe(onEach: (T) -> Unit): Watch {
        /*
         * Why: undispatched on Main.immediate, so the collector is subscribed before observe returns. A UiAction
         * emitted right after (no replay) is not lost, and a StateFlow's value reaches the first frame.
         */
        val job = CoroutineScope(
            Dispatchers.Main.immediate + loggingHandler,
        ).launch(start = CoroutineStart.UNDISPATCHED) {
            flow.collect(onEach::invoke)
        }
        return Watch(job::cancel)
    }
}
