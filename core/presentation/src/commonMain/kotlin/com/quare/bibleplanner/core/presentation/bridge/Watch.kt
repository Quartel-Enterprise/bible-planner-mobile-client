package com.quare.bibleplanner.core.presentation.bridge

// Why: what Swift and JS hold to stop observing a flow when their view goes away.
fun interface Watch {
    fun cancel()
}
