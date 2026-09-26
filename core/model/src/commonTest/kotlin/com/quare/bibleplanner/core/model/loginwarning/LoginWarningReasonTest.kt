package com.quare.bibleplanner.core.model.loginwarning

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class LoginWarningReasonTest {
    private val reasons = listOf(
        LoginWarningReason.Purchase,
        LoginWarningReason.DayStudy,
        LoginWarningReason.AiChat,
        LoginWarningReason.Preferences.Theme,
        LoginWarningReason.Preferences.Language,
        LoginWarningReason.Preferences.StudySuggestion,
    )

    @Test
    fun `GIVEN every reason WHEN carrying it by key THEN restores the same reason`() {
        // When
        val restored = reasons.map { LoginWarningReason.fromKey(it.key) }

        // Then
        assertEquals(reasons, restored)
    }

    @Test
    fun `GIVEN every reason WHEN reading its key THEN each key is stable and unique`() {
        // When
        val keys = reasons.map(LoginWarningReason::key)

        // Then
        assertEquals(
            listOf(
                "purchase",
                "day_study",
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
        // When / Then
        assertFailsWith<NoSuchElementException> { LoginWarningReason.fromKey("unknown") }
    }
}
