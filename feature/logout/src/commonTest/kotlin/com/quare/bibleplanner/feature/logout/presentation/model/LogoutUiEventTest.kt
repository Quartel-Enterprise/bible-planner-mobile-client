package com.quare.bibleplanner.feature.logout.presentation.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LogoutUiEventTest {
    @Test
    fun `GIVEN OnConfirmLogout WHEN reading shouldFlushPending THEN it flushes pending changes`() {
        // Given
        val event = LogoutUiEvent.ConfirmLogoutClick.OnConfirmLogout

        // When
        val shouldFlushPending = event.shouldFlushPending

        // Then
        assertTrue(shouldFlushPending)
    }

    @Test
    fun `GIVEN OnForceLogout WHEN reading shouldFlushPending THEN it skips flushing pending changes`() {
        // Given
        val event = LogoutUiEvent.ConfirmLogoutClick.OnForceLogout

        // When
        val shouldFlushPending = event.shouldFlushPending

        // Then
        assertFalse(shouldFlushPending)
    }
}
