package com.quare.bibleplanner.core.model.loginwarning

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class LoginWarningReasonTest {
    private val reasons = listOf(
        LoginWarningReason.Purchase,
        LoginWarningReason.DayStudy,
        LoginWarningReason.ChapterStudy,
        LoginWarningReason.AiChat,
        LoginWarningReason.Preferences.Theme,
        LoginWarningReason.Preferences.Language,
        LoginWarningReason.Preferences.StudySuggestion,
    )

    @Test
    fun `GIVEN every reason WHEN carrying it by key THEN restores the same reason`() {
        // Given
        val keys = reasons.map(LoginWarningReason::key)

        // When
        val restored = keys.map(LoginWarningReason.Companion::fromKey)

        // Then
        assertEquals(reasons, restored)
    }

    @Test
    fun `GIVEN every reason WHEN reading its key THEN each key is stable and unique`() {
        // Given
        val everyReason = reasons

        // When
        val keys = everyReason.map(LoginWarningReason::key)

        // Then
        assertEquals(
            listOf(
                "purchase",
                "day_study",
                "chapter_study",
                "ai_chat",
                "preferences_theme",
                "preferences_language",
                "preferences_study_suggestion",
            ),
            keys,
        )
    }

    @Test
    fun `GIVEN an unknown key WHEN restoring the reason THEN fails`() {
        // Given
        val key = "unknown"

        // When
        val result = runCatching { LoginWarningReason.fromKey(key) }

        // Then
        assertIs<NoSuchElementException>(result.exceptionOrNull())
    }
}
