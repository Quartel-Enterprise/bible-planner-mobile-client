package com.quare.bibleplanner.tools.agentcli.storage

import java.io.File
import java.util.prefs.Preferences
import java.util.prefs.PreferencesFactory

/*
 * Why: the JDK's own file-backed factory needs a native library macOS builds don't ship, so
 * java.util.prefs is pointed at this one through the java.util.prefs.PreferencesFactory property.
 */
class FilePreferencesFactory : PreferencesFactory {
    private val root: Preferences by lazy {
        FilePreferences(
            directory = File(requireNotNull(System.getProperty(ROOT_PROPERTY)) { "$ROOT_PROPERTY is not set" }),
            parent = null,
            name = "",
        )
    }

    override fun userRoot(): Preferences = root

    override fun systemRoot(): Preferences = root

    companion object {
        const val ROOT_PROPERTY = "bibleplanner.agentcli.preferences"
    }
}
