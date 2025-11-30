package com.quare.bibleplanner.core.provider.platform

actual fun getPlatform(): Platform {
    val osName = System.getProperty("os.name", "").lowercase()
    return when {
        osName.contains("mac") -> Platform.MACOS
        osName.contains("linux") -> Platform.LINUX
        osName.contains("windows") -> Platform.WINDOWS
        else -> Platform.LINUX // Default fallback
    }
}
