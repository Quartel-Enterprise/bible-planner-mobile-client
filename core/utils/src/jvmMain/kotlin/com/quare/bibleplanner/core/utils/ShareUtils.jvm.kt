package com.quare.bibleplanner.core.utils

actual fun shareContent(
    message: String,
    imageBytes: ByteArray?,
) {
    // No-op for JVM
    println("Sharing on JVM: $message")
}
