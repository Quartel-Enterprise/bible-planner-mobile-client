package com.quare.bibleplanner.tools.agentcli.storage

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

internal class FilePreferencesTest {
    private lateinit var directory: File

    @BeforeTest
    fun setUp() {
        directory = Files.createTempDirectory("prefs").toFile()
    }

    @AfterTest
    fun tearDown() {
        directory.deleteRecursively()
    }

    @Test
    fun `GIVEN a saved value WHEN opening a new root on the same directory THEN the value is still there`() {
        // Given
        createRoot().node("com/quare/billing").put("id", "abc")

        // When
        val restored = createRoot().node("com/quare/billing").get("id", null)

        // Then
        assertEquals(
            expected = "abc",
            actual = restored,
        )
    }

    @Test
    fun `GIVEN keys and children WHEN listing and removing them THEN reflects each change`() {
        // Given
        val root = createRoot()
        root.put("session", "token")
        root.put("other", "value")
        root.node("child/name").put("key", "value")

        // When
        root.remove("other")
        val keys = root.keys().toList()
        val children = root.childrenNames().toList()
        root.node("child").removeNode()

        // Then
        assertEquals(
            expected = listOf("session"),
            actual = keys,
        )
        assertEquals(
            expected = listOf("child"),
            actual = children,
        )
        assertFalse(createRoot().nodeExists("child"))
        assertNull(createRoot().get("other", null))
    }

    @Test
    fun `GIVEN the factory WHEN asking for the user and system roots THEN serves the same root`() {
        // Given
        System.setProperty(FilePreferencesFactory.ROOT_PROPERTY, directory.absolutePath)
        val factory = FilePreferencesFactory()

        // When
        val isSameRoot = factory.userRoot() === factory.systemRoot()

        // Then
        assertEquals(
            expected = true,
            actual = isSameRoot,
        )
    }

    private fun createRoot(): FilePreferences = FilePreferences(
        parent = null,
        name = "",
        directory = directory,
    ).also { it.sync() }
}
