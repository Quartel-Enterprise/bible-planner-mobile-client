package com.quare.bibleplanner.core.utils

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class DesktopEnvironmentTest {
    private val originalOsName: String = System.getProperty("os.name")
    private val originalUserHome: String = System.getProperty("user.home")

    private lateinit var home: File

    @BeforeTest
    fun setUp() {
        home = Files.createTempDirectory("home").toFile()
        System.setProperty("user.home", home.absolutePath)
    }

    @AfterTest
    fun tearDown() {
        System.setProperty("os.name", originalOsName)
        System.setProperty("user.home", originalUserHome)
        home.deleteRecursively()
    }

    @Test
    fun `GIVEN each os name WHEN detecting the desktop os THEN falls back to linux for unknown ones`() {
        // When
        val detected = listOf("Mac OS X", "Windows 11", "Linux", "FreeBSD").map { osName ->
            System.setProperty("os.name", osName)
            DesktopOs.detect()
        }

        // Then
        assertEquals(
            listOf(
                DesktopOs.MAC,
                DesktopOs.WINDOWS,
                DesktopOs.LINUX,
                DesktopOs.LINUX,
            ),
            detected,
        )
    }

    @Test
    fun `GIVEN macOS WHEN resolving the app data directory THEN creates it under Application Support`() {
        // Given
        System.setProperty("os.name", "Mac OS X")

        // When
        val directory = resolveAppDataDirectory()

        // Then
        assertEquals(File(home, "Library/Application Support/com.quare.bibleplanner"), directory)
        assertTrue(directory.isDirectory)
    }

    @Test
    fun `GIVEN Windows WHEN resolving the app data directory THEN creates it under the roaming app data`() {
        // Given
        System.setProperty("os.name", "Windows 11")
        val appDataRoot = System.getenv("APPDATA")?.takeIf(String::isNotBlank)?.let(::File)
            ?: File(home, "AppData/Roaming")

        // When
        val directory = resolveAppDataDirectory()

        // Then
        assertEquals(File(appDataRoot, "com.quare.bibleplanner").absoluteFile, directory.absoluteFile)
        assertTrue(directory.isDirectory)
    }

    @Test
    fun `GIVEN Linux WHEN resolving the app data directory THEN creates it under the XDG data home`() {
        // Given
        System.setProperty("os.name", "Linux")
        val dataRoot = System.getenv("XDG_DATA_HOME")?.takeIf(String::isNotBlank)?.let(::File)
            ?: File(home, ".local/share")

        // When
        val directory = resolveAppDataDirectory()

        // Then
        assertEquals(File(dataRoot, "com.quare.bibleplanner").absoluteFile, directory.absoluteFile)
        assertTrue(directory.isDirectory)
    }

    @Test
    fun `GIVEN macOS WHEN resolving the app cache directory THEN creates it under Library Caches`() {
        // Given
        System.setProperty("os.name", "Mac OS X")

        // When
        val directory = resolveAppCacheDirectory()

        // Then
        assertEquals(File(home, "Library/Caches/com.quare.bibleplanner"), directory)
        assertTrue(directory.isDirectory)
    }

    @Test
    fun `GIVEN Windows WHEN resolving the app cache directory THEN creates a Cache folder under the local app data`() {
        // Given
        System.setProperty("os.name", "Windows 11")
        val localAppDataRoot = System.getenv("LOCALAPPDATA")?.takeIf(String::isNotBlank)?.let(::File)
            ?: File(home, "AppData/Local")

        // When
        val directory = resolveAppCacheDirectory()

        // Then
        assertEquals(File(localAppDataRoot, "com.quare.bibleplanner/Cache").absoluteFile, directory.absoluteFile)
        assertTrue(directory.isDirectory)
    }

    @Test
    fun `GIVEN Linux WHEN resolving the app cache directory THEN creates it under the XDG cache home`() {
        // Given
        System.setProperty("os.name", "Linux")
        val cacheRoot = System.getenv("XDG_CACHE_HOME")?.takeIf(String::isNotBlank)?.let(::File)
            ?: File(home, ".cache")

        // When
        val directory = resolveAppCacheDirectory()

        // Then
        assertEquals(File(cacheRoot, "com.quare.bibleplanner").absoluteFile, directory.absoluteFile)
        assertTrue(directory.isDirectory)
    }

    @Test
    fun `GIVEN an unknown os WHEN resolving the app cache directory THEN uses the linux cache location`() {
        // Given
        System.setProperty("os.name", "FreeBSD")
        val cacheRoot = System.getenv("XDG_CACHE_HOME")?.takeIf(String::isNotBlank)?.let(::File)
            ?: File(home, ".cache")

        // When
        val directory = resolveAppCacheDirectory()

        // Then
        assertEquals(File(cacheRoot, "com.quare.bibleplanner").absoluteFile, directory.absoluteFile)
        assertTrue(directory.isDirectory)
    }

    @Test
    fun `GIVEN a cache directory with content WHEN resolving it again THEN returns it and keeps the content`() {
        // Given
        System.setProperty("os.name", "Mac OS X")
        val existing = File(home, "Library/Caches/com.quare.bibleplanner").apply { mkdirs() }
        val cachedAudio = File(existing, "chapter.mp3").apply { writeText("audio") }

        // When
        val directory = resolveAppCacheDirectory()

        // Then
        assertEquals(existing, directory)
        assertEquals("audio", cachedAudio.readText())
    }

    @Test
    fun `GIVEN the app data directory on each os WHEN resolving the app cache directory THEN never shares it`() {
        // Given
        val osNames = listOf("Mac OS X", "Windows 11", "Linux")

        // When
        val directoryPairs = osNames.map { osName ->
            System.setProperty("os.name", osName)
            resolveAppDataDirectory().absoluteFile to resolveAppCacheDirectory().absoluteFile
        }

        // Then
        directoryPairs.forEach { (dataDirectory, cacheDirectory) ->
            assertNotEquals(dataDirectory, cacheDirectory)
            assertFalse(cacheDirectory.startsWith(dataDirectory))
        }
    }

    @Test
    fun `GIVEN a legacy file and no target WHEN migrating THEN moves the content to the target`() {
        // Given
        val legacy = File(home, "legacy.db").apply { writeText("data") }
        val target = File(home, "target.db")

        // When
        migrateLegacyFileIfPresent(
            legacyFile = legacy,
            targetFile = target,
        )

        // Then
        assertEquals("data", target.readText())
        assertFalse(legacy.exists())
    }

    @Test
    fun `GIVEN an existing target WHEN migrating THEN keeps both files untouched`() {
        // Given
        val legacy = File(home, "legacy.db").apply { writeText("old") }
        val target = File(home, "target.db").apply { writeText("new") }

        // When
        migrateLegacyFileIfPresent(
            legacyFile = legacy,
            targetFile = target,
        )

        // Then
        assertEquals("new", target.readText())
        assertEquals("old", legacy.readText())
    }

    @Test
    fun `GIVEN no legacy file WHEN migrating THEN creates nothing`() {
        // Given
        val target = File(home, "target.db")

        // When
        migrateLegacyFileIfPresent(
            legacyFile = File(home, "missing.db"),
            targetFile = target,
        )

        // Then
        assertFalse(target.exists())
    }
}
