package com.quare.bibleplanner.core.provider.platform

import kotlin.test.Test
import kotlin.test.assertIs

internal class PlatformDetectorTest {
    @Test
    fun `GIVEN the desktop runtime WHEN detecting the platform THEN reports a desktop platform`() {
        // When
        val platform = getPlatform()

        // Then
        assertIs<Platform.Desktop>(platform)
    }
}
