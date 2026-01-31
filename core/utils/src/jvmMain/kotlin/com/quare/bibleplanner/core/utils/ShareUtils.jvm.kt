package com.quare.bibleplanner.core.utils

import co.touchlab.kermit.Logger

actual fun shareContent(
    message: String,
    imageBytes: ByteArray?,
) {
    // No-op for JVM
    Logger.d { "Sharing on JVM: $message" }
}
