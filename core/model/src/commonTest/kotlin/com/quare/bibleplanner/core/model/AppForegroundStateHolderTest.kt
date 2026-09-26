package com.quare.bibleplanner.core.model

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AppForegroundStateHolderTest {
    private lateinit var holder: AppForegroundStateHolder

    @BeforeTest
    fun setUp() {
        holder = AppForegroundStateHolder()
    }

    @Test
    fun `GIVEN a fresh holder WHEN reading it THEN the app starts in the background`() {
        // Then
        assertFalse(holder.isForeground.value)
    }

    @Test
    fun `GIVEN the app comes to the foreground WHEN reading the holder THEN it is foreground`() {
        // When
        holder.onForegrounded()

        // Then
        assertTrue(holder.isForeground.value)
    }

    @Test
    fun `GIVEN a foreground app WHEN it goes to the background THEN it is no longer foreground`() {
        // Given
        holder.onForegrounded()

        // When
        holder.onBackgrounded()

        // Then
        assertFalse(holder.isForeground.value)
    }
}
