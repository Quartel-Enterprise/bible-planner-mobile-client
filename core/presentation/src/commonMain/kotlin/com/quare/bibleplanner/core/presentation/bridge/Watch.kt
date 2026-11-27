package com.quare.bibleplanner.core.presentation.bridge

// Why: Swift and JS stop observing through this instead of the coroutine Job, which they can't hold or cancel.
fun interface Watch {
    fun cancel()
}
