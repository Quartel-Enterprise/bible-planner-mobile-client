package com.quare.bibleplanner.core.utils

import java.io.File

private const val APP_DATA_DIRECTORY_NAME = "com.quare.bibleplanner"

fun resolveAppDataDirectory(): File {
    val userHome = System.getProperty("user.home")
    val osName = System.getProperty("os.name").lowercase()
    val dataDirectory = when {
        osName.contains("mac") -> File(userHome, "Library/Application Support/$APP_DATA_DIRECTORY_NAME")
        osName.contains("win") -> File(windowsAppDataRoot(userHome), APP_DATA_DIRECTORY_NAME)
        else -> File(linuxDataRoot(userHome), APP_DATA_DIRECTORY_NAME)
    }
    dataDirectory.mkdirs()
    return dataDirectory
}

fun migrateLegacyFileIfPresent(
    legacyFile: File,
    targetFile: File,
) {
    if (!legacyFile.exists() || targetFile.exists()) return
    legacyFile.copyTo(targetFile)
    legacyFile.delete()
}

private fun windowsAppDataRoot(userHome: String): String = System.getenv("APPDATA")?.takeIf(String::isNotBlank)
    ?: File(userHome, "AppData/Roaming").absolutePath

private fun linuxDataRoot(userHome: String): String = System.getenv("XDG_DATA_HOME")?.takeIf(String::isNotBlank)
    ?: File(userHome, ".local/share").absolutePath
