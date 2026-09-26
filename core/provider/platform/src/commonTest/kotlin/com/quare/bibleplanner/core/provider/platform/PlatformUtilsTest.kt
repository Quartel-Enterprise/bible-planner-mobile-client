package com.quare.bibleplanner.core.provider.platform

import kotlin.test.Test
import kotlin.test.assertEquals

internal class PlatformUtilsTest {
    private val platforms = listOf(
        Platform.Android,
        Platform.Ios,
        Platform.Desktop.MacOs,
        Platform.Desktop.Linux,
        Platform.Desktop.Windows,
    )

    @Test
    fun `GIVEN every platform WHEN checking for Apple THEN only iOS and macOS are Apple`() {
        // When
        val applePlatforms = platforms.filter(Platform::isApple)

        // Then
        assertEquals(listOf(Platform.Ios, Platform.Desktop.MacOs), applePlatforms)
    }

    @Test
    fun `GIVEN every platform WHEN checking for Android THEN only Android is Android`() {
        // When
        val androidPlatforms = platforms.filter(Platform::isAndroid)

        // Then
        assertEquals(listOf<Platform>(Platform.Android), androidPlatforms)
    }

    @Test
    fun `GIVEN every platform WHEN checking for desktop THEN only the three desktop systems are desktop`() {
        // When
        val desktopPlatforms = platforms.filter(Platform::isDesktop)

        // Then
        assertEquals(
            listOf(Platform.Desktop.MacOs, Platform.Desktop.Linux, Platform.Desktop.Windows),
            desktopPlatforms,
        )
    }
}
