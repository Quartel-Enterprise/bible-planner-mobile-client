package com.quare.bibleplanner.tools.agentcli.storage

import java.io.File

private const val MARKER_FILE = ".agent-cli"
private val dataRootVariables = listOf("XDG_DATA_HOME", "APPDATA")

/*
 * Why: on the JVM the app keeps Room and DataStore under user.home (or XDG_DATA_HOME/APPDATA), moves
 * legacy files in from java.io.tmpdir and user.dir, and keeps the Supabase session, the analytics
 * client id and the RevenueCat id in java.util.prefs, which on macOS is one plist shared with the
 * desktop app. Pointing all of them at the data directory keeps the agent from reading, moving or
 * signing out the developer's desktop app, and keeps parallel worktrees apart. It has to run before
 * anything touches them.
 */
fun isolateStorage(
    dataDirectory: File,
    isFresh: Boolean,
    environment: Map<String, String>,
) {
    val root = dataDirectory.absoluteFile
    if (isFresh && root.exists()) {
        require(File(root, MARKER_FILE).exists() || root.list().isNullOrEmpty()) {
            "refusing to delete $root: the agent CLI did not create it"
        }
        root.deleteRecursively()
    }
    dataRootVariables.forEach { variable ->
        val value = environment[variable] ?: return@forEach
        require(File(value).absoluteFile.startsWith(root)) {
            "$variable points at $value, outside $root: the app would use the developer's data there. " +
                "Start the agent CLI through scripts/agent-cli.sh, or unset $variable"
        }
    }
    val home = File(root, "home").apply { mkdirs() }
    File(root, MARKER_FILE).createNewFile()
    System.setProperty("user.home", home.absolutePath)
    System.setProperty("user.dir", File(root, "work").apply { mkdirs() }.absolutePath)
    System.setProperty("java.io.tmpdir", File(root, "tmp").apply { mkdirs() }.absolutePath)
    System.setProperty(FilePreferencesFactory.ROOT_PROPERTY, File(root, "prefs").apply { mkdirs() }.absolutePath)
    System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory::class.java.name)
}
