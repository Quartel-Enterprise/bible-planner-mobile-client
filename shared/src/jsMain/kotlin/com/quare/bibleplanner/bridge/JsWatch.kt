@file:JsExport

package com.quare.bibleplanner.bridge

/*
 * Why: :core:presentation's Watch is a fun interface in commonMain, which can't be exported while the module also
 * builds for wasmJs. React stops observing through this instead.
 */
class JsWatch internal constructor(
    private val onCancel: () -> Unit,
) {
    fun cancel() {
        onCancel()
    }
}
