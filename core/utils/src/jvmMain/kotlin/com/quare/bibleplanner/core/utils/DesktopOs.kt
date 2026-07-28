package com.quare.bibleplanner.core.utils

enum class DesktopOs {
    MAC,
    WINDOWS,
    LINUX,
    ;

    companion object {
        fun detect(): DesktopOs {
            val osName = System.getProperty("os.name", "").lowercase()
            return when {
                osName.contains("mac") -> MAC
                osName.contains("windows") -> WINDOWS
                else -> LINUX
            }
        }
    }
}
