package com.quare.bibleplanner.core.provider.platform

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DebugBuildTest {
    @AfterTest
    fun tearDown() {
        System.clearProperty(PACKAGED_APP_PATH_PROPERTY)
    }

    @Test
    fun `GIVEN the app runs from gradle WHEN checking the build THEN it is a debug build`() {
        // Given
        System.clearProperty(PACKAGED_APP_PATH_PROPERTY)

        // When
        val isDebug = isDebugBuild()

        // Then
        assertTrue(isDebug)
    }

    @Test
    fun `GIVEN the app runs from a jpackage installer WHEN checking the build THEN it is a release build`() {
        // Given
        System.setProperty(PACKAGED_APP_PATH_PROPERTY, "/Applications/Bible Planner.app")

        // When
        val isDebug = isDebugBuild()

        // Then
        assertFalse(isDebug)
    }

    private companion object {
        const val PACKAGED_APP_PATH_PROPERTY = "jpackage.app-path"
    }
}
