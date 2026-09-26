package com.quare.bibleplanner.feature.chat.presentation

import bibleplanner.feature.chat.generated.resources.Res
import bibleplanner.feature.chat.generated.resources.chat_default_suggestion_application
import bibleplanner.feature.chat.generated.resources.chat_default_suggestion_context
import bibleplanner.feature.chat.generated.resources.chat_default_suggestion_grace
import bibleplanner.feature.chat.generated.resources.chat_default_suggestion_plan
import bibleplanner.feature.chat.generated.resources.chat_default_suggestion_start
import bibleplanner.feature.chat.generated.resources.chat_default_suggestion_summary
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetDefaultChatSuggestionsUseCaseTest {
    private lateinit var useCase: GetDefaultChatSuggestionsUseCase

    @BeforeTest
    fun setUp() {
        useCase = GetDefaultChatSuggestionsUseCase()
    }

    @Test
    fun `GIVEN a reading WHEN asking for starters THEN offers the questions about the reading`() = runTest {
        // When
        val suggestions = useCase(hasReadingContext = true)

        // Then
        assertEquals(
            expected = listOf(
                getString(Res.string.chat_default_suggestion_summary),
                getString(Res.string.chat_default_suggestion_context),
                getString(Res.string.chat_default_suggestion_application),
            ),
            actual = suggestions,
        )
    }

    @Test
    fun `GIVEN no reading WHEN asking for starters THEN offers the open questions`() = runTest {
        // When
        val suggestions = useCase(hasReadingContext = false)

        // Then
        assertEquals(
            expected = listOf(
                getString(Res.string.chat_default_suggestion_start),
                getString(Res.string.chat_default_suggestion_plan),
                getString(Res.string.chat_default_suggestion_grace),
            ),
            actual = suggestions,
        )
    }
}
