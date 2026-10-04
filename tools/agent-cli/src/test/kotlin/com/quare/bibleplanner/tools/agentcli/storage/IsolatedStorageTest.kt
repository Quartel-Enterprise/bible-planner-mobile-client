package com.quare.bibleplanner.tools.agentcli.storage

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class IsolatedStorageTest {
    private val directory: File = Files.createTempDirectory("agent-cli").toFile()
    private val savedProperties = listOf(
        "user.home",
        "user.dir",
        "java.io.tmpdir",
        FilePreferencesFactory.ROOT_PROPERTY,
        "java.util.prefs.PreferencesFactory",
    ).associateWith(System::getProperty)

    @AfterTest
    fun tearDown() {
        savedProperties.forEach { (name, value) ->
            if (value == null) System.clearProperty(name) else System.setProperty(name, value)
        }
        directory.deleteRecursively()
    }

    @Test
    fun `GIVEN a data directory WHEN isolating storage THEN every place the app keeps data points inside it`() {
        // When
        isolateStorage(
            dataDirectory = directory,
            isFresh = false,
            environment = mapOf("XDG_DATA_HOME" to File(directory, "home/.local/share").path),
        )

        // Then
        assertEquals(
            expected = listOf(
                File(directory, "home").absolutePath,
                File(directory, "work").absolutePath,
                File(directory, "tmp").absolutePath,
                File(directory, "prefs").absolutePath,
                FilePreferencesFactory::class.java.name,
            ),
            actual = savedProperties.keys.map(System::getProperty),
        )
    }

    @Test
    fun `GIVEN a data directory the agent CLI created WHEN starting fresh THEN empties it`() {
        // Given
        isolateStorage(
            dataDirectory = directory,
            isFresh = false,
            environment = emptyMap(),
        )
        val leftover = File(directory, "home/old.db").apply { writeText("old") }

        // When
        isolateStorage(
            dataDirectory = directory,
            isFresh = true,
            environment = emptyMap(),
        )

        // Then
        assertFalse(leftover.exists())
        assertTrue(File(directory, "home").isDirectory)
    }

    @Test
    fun `GIVEN a directory holding other files WHEN starting fresh THEN refuses to delete it`() {
        // Given
        val document = File(directory, "notes.txt").apply { writeText("keep me") }

        // When
        val error = assertFailsWith<IllegalArgumentException> {
            isolateStorage(
                dataDirectory = directory,
                isFresh = true,
                environment = emptyMap(),
            )
        }

        // Then
        assertEquals(
            expected = "refusing to use ${directory.absoluteFile}: it holds files the agent CLI did not create",
            actual = error.message,
        )
        assertTrue(document.exists())
    }

    @Test
    fun `GIVEN a directory holding other files WHEN starting without fresh THEN refuses it and never marks it`() {
        // Given
        File(directory, "notes.txt").writeText("keep me")

        // When
        assertFailsWith<IllegalArgumentException> {
            isolateStorage(
                dataDirectory = directory,
                isFresh = false,
                environment = emptyMap(),
            )
        }

        // Then
        assertFalse(File(directory, ".agent-cli").exists())
    }

    @Test
    fun `GIVEN a data root outside the data directory WHEN isolating storage THEN refuses to start`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> {
            isolateStorage(
                dataDirectory = directory,
                isFresh = false,
                environment = mapOf("APPDATA" to "/Users/someone/AppData/Roaming"),
            )
        }

        // Then
        assertTrue(error.message.orEmpty().startsWith("APPDATA points at /Users/someone/AppData/Roaming, outside"))
    }
}
