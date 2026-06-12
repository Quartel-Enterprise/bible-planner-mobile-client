package com.quare.bibleplanner.core.provider.platform

internal actual fun getPlatform(): Platform {
    val osName = System.getProperty("os.name", "").lowercase()
    return when {
        osName.contains("mac") -> Platform.Desktop.MacOs
        osName.contains("linux") -> Platform.Desktop.Linux
        osName.contains("windows") -> Platform.Desktop.Windows
        else -> Platform.Desktop.Linux
    }
}
