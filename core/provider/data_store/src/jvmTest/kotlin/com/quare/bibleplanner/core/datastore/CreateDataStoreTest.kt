package com.quare.bibleplanner.core.datastore

import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.test.runTest
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CreateDataStoreTest {
    private val themeKey = stringPreferencesKey("theme")
    private lateinit var sandbox: File
    private lateinit var originalUserHome: String
    private lateinit var originalUserDir: String

    @BeforeTest
    fun setUp() {
        sandbox = Files.createTempDirectory("datastore").toFile()
        originalUserHome = System.getProperty(USER_HOME)
        originalUserDir = System.getProperty(USER_DIR)
    }

    @AfterTest
    fun tearDown() {
        System.setProperty(USER_HOME, originalUserHome)
        System.setProperty(USER_DIR, originalUserDir)
        sandbox.deleteRecursively()
    }

    @Test
    fun `GIVEN a directory WHEN creating the common data store THEN persists the preferences file in it`() = runTest {
        // Given
        val dataStore = createCommonDataStore { fileName -> File(sandbox, fileName).absolutePath }

        // When
        dataStore.write(
            key = themeKey,
            value = "dark",
        )

        // Then
        assertTrue(File(sandbox, DATA_STORE_FILE_NAME).exists())
        assertEquals("dark", dataStore.read(themeKey))
    }

    @Test
    fun `GIVEN legacy preferences in the working directory WHEN creating the data store THEN moves them`() = runTest {
        // Given
        val workingDirectory = File(sandbox, "working").apply { mkdirs() }
        val legacyStore = createCommonDataStore { fileName -> File(workingDirectory, fileName).absolutePath }
        legacyStore.write(
            key = themeKey,
            value = "light",
        )
        System.setProperty(USER_HOME, File(sandbox, "home").absolutePath)
        System.setProperty(USER_DIR, workingDirectory.absolutePath)

        // When
        val dataStore = createDataStore()

        // Then
        assertEquals("light", dataStore.read(themeKey))
        assertFalse(File(workingDirectory, DATA_STORE_FILE_NAME).exists())
    }

    private companion object {
        const val USER_HOME = "user.home"
        const val USER_DIR = "user.dir"
        const val DATA_STORE_FILE_NAME = "prefs.preferences_pb"
    }
}
