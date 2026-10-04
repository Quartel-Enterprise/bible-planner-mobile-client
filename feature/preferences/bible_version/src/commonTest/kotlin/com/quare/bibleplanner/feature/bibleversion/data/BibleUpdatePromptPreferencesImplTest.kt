package com.quare.bibleplanner.feature.bibleversion.data

import androidx.datastore.preferences.core.emptyPreferences
import com.quare.bibleplanner.core.provider.datastore.testing.FakePreferencesDataStore
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DismissBibleUpdatePromptUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class BibleUpdatePromptPreferencesImplTest {
    private lateinit var preferences: BibleUpdatePromptPreferencesImpl

    @BeforeTest
    fun setUp() {
        preferences = BibleUpdatePromptPreferencesImpl(FakePreferencesDataStore(emptyPreferences()))
    }

    @Test
    fun `GIVEN a prompt never dismissed WHEN reading the last dismissal THEN returns null`() = runTest {
        // When
        val lastDismissedAt = preferences.getLastDismissedAt()

        // Then
        assertNull(lastDismissedAt)
    }

    @Test
    fun `GIVEN the dismiss use case WHEN dismissing the prompt THEN remembers when it was dismissed`() = runTest {
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
