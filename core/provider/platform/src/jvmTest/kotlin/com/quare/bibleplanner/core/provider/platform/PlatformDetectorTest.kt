package com.quare.bibleplanner.core.provider.platform

import kotlin.test.Test
import kotlin.test.assertIs

internal class PlatformDetectorTest {
    @Test
    fun `GIVEN the desktop runtime WHEN detecting the platform THEN reports a desktop platform`() {
        // Given
        val detectPlatform = ::getPlatform

        // When
        val platform = detectPlatform()

        // Then
        assertIs<Platform.Desktop>(platform)
    }
}
