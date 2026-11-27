package com.quare.bibleplanner.core.utils

import co.touchlab.kermit.Logger

actual fun shareContent(
    message: String,
    imageBytes: ByteArray?,
) {
    Logger.d { "Sharing on the web: $message" }
}
