package com.quare.bibleplanner.core.provider.platform.notification

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class JvmNotificationPermissionRequesterTest {
    private val requester = JvmNotificationPermissionRequester()

    @Test
    fun `GIVEN the desktop app WHEN asking whether it can prompt THEN never prompts`() = runTest {
        // When
        val canPrompt = requester.canPrompt()

        // Then
        assertFalse(canPrompt)
    }

    @Test
    fun `GIVEN the desktop app WHEN requesting the permission THEN reports it denied`() = runTest {
        // When
        val result = requester.request()

        // Then
        assertEquals(NotificationPermissionPromptResult.DENIED, result)
    }
}
