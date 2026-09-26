package com.quare.bibleplanner.feature.loginwarning.presentation

import bibleplanner.feature.login_warning.generated.resources.Res
import bibleplanner.feature.login_warning.generated.resources.login_warning_message_ai_chat
import bibleplanner.feature.login_warning.generated.resources.login_warning_message_day_study
import bibleplanner.feature.login_warning.generated.resources.login_warning_message_language
import bibleplanner.feature.login_warning.generated.resources.login_warning_message_purchase
import bibleplanner.feature.login_warning.generated.resources.login_warning_message_study_suggestion
import bibleplanner.feature.login_warning.generated.resources.login_warning_message_theme
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LoginWarningMessageTest {
    @Test
    fun `GIVEN each reason to log in WHEN picking the warning message THEN explains that reason`() {
        // Given
        val reasons = listOf(
            LoginWarningReason.Purchase,
            LoginWarningReason.DayStudy,
            LoginWarningReason.AiChat,
            LoginWarningReason.Preferences.Theme,
            LoginWarningReason.Preferences.Language,
            LoginWarningReason.Preferences.StudySuggestion,
        )

        // When
        val messages = reasons.map(LoginWarningReason::toMessageResource)

        // Then
        assertEquals(
            listOf(
                Res.string.login_warning_message_purchase,
                Res.string.login_warning_message_day_study,
                Res.string.login_warning_message_ai_chat,
                Res.string.login_warning_message_theme,
                Res.string.login_warning_message_language,
                Res.string.login_warning_message_study_suggestion,
            ),
            messages,
        )
    }
}
