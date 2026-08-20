package com.quare.bibleplanner.core.utils

import java.io.File

private const val APP_DATA_DIRECTORY_NAME = "com.quare.bibleplanner"

fun resolveAppDataDirectory(): File {
    val userHome = System.getProperty("user.home")
    val dataDirectory = when (DesktopOs.detect()) {
        DesktopOs.MAC -> File(userHome, "Library/Application Support/$APP_DATA_DIRECTORY_NAME")
        DesktopOs.WINDOWS -> File(getWindowsAppDataRoot(userHome), APP_DATA_DIRECTORY_NAME)
        DesktopOs.LINUX -> File(getLinuxDataRoot(userHome), APP_DATA_DIRECTORY_NAME)
    }
    dataDirectory.mkdirs()
    return dataDirectory
}

private fun getWindowsAppDataRoot(userHome: String): String = System.getenv("APPDATA")?.takeIf(String::isNotBlank)
    ?: File(userHome, "AppData/Roaming").absolutePath

private fun getLinuxDataRoot(userHome: String): String = System.getenv("XDG_DATA_HOME")?.takeIf(String::isNotBlank)
    ?: File(userHome, ".local/share").absolutePath
