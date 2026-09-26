package com.quare.bibleplanner.feature.bibleversion.data

import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DismissBibleUpdatePromptUseCase
import com.quare.bibleplanner.feature.bibleversion.fake.FakePreferencesDataStore
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class BibleUpdatePromptPreferencesImplTest {
    private lateinit var preferences: BibleUpdatePromptPreferencesImpl

    @BeforeTest
    fun setUp() {
        preferences = BibleUpdatePromptPreferencesImpl(FakePreferencesDataStore())
    }

    @Test
    fun `has no dismissal before the prompt is ever dismissed`() = runTest {
        // When
        val lastDismissedAt = preferences.getLastDismissedAt()

        // Then
        assertNull(lastDismissedAt)
    }

    @Test
    fun `remembers when the prompt was dismissed`() = runTest {
        // Given
        val dismissPrompt = DismissBibleUpdatePromptUseCase(
            bibleUpdatePromptPreferences = preferences,
            currentTimestampProvider = { NOW },
        )

        // When
        dismissPrompt()

        // Then
        assertEquals(
            expected = NOW,
            actual = preferences.getLastDismissedAt(),
        )
    }

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}
