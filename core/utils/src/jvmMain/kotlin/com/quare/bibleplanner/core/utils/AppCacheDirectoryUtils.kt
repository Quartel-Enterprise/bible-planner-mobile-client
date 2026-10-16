package com.quare.bibleplanner.core.utils

import java.io.File

// Why: files the app can download again belong where the OS and backups treat them as disposable.
fun resolveAppCacheDirectory(): File {
    val userHome = System.getProperty("user.home")
    val cacheDirectory = when (DesktopOs.detect()) {
        DesktopOs.MAC -> File(userHome, "Library/Caches/$APP_DATA_DIRECTORY_NAME")
        DesktopOs.WINDOWS -> File(getWindowsLocalAppDataRoot(userHome), "$APP_DATA_DIRECTORY_NAME/Cache")
        DesktopOs.LINUX -> File(getLinuxCacheRoot(userHome), APP_DATA_DIRECTORY_NAME)
    }
    cacheDirectory.mkdirs()
    return cacheDirectory
}

private fun getWindowsLocalAppDataRoot(userHome: String): String = System
    .getenv("LOCALAPPDATA")
    ?.takeIf(String::isNotBlank)
    ?: File(userHome, "AppData/Local").absolutePath

private fun getLinuxCacheRoot(userHome: String): String = System
    .getenv("XDG_CACHE_HOME")
    ?.takeIf(String::isNotBlank)
    ?: File(userHome, ".cache").absolutePath
