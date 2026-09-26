package com.quare.bibleplanner.core.review.data

import androidx.datastore.preferences.core.emptyPreferences
import com.quare.bibleplanner.core.review.fake.FakePreferencesDataStore
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ReviewPreferencesImplTest {
    private lateinit var preferences: ReviewPreferencesImpl

    @Test
    fun `GIVEN a fresh install WHEN reading the review history THEN nothing is recorded`() = runTest {
        // When
        val firstEligibleAt = preferences.getFirstEligibleAt()
        val lastPromptedAt = preferences.getLastPromptedAt()
        val lastPromptedVersion = preferences.getLastPromptedVersion()

        // Then
        assertNull(firstEligibleAt)
        assertNull(lastPromptedAt)
        assertNull(lastPromptedVersion)
    }

    @Test
    fun `GIVEN a recorded review history WHEN reading it back THEN returns each stored value`() = runTest {
        // Given
        preferences.setFirstEligibleAt(FIRST_ELIGIBLE_AT)
        preferences.setLastPromptedAt(LAST_PROMPTED_AT)
        preferences.setLastPromptedVersion(VERSION)

        // When
        val firstEligibleAt = preferences.getFirstEligibleAt()
        val lastPromptedAt = preferences.getLastPromptedAt()
        val lastPromptedVersion = preferences.getLastPromptedVersion()

        // Then
        assertEquals(FIRST_ELIGIBLE_AT, firstEligibleAt)
        assertEquals(LAST_PROMPTED_AT, lastPromptedAt)
        assertEquals(VERSION, lastPromptedVersion)
    }

    @BeforeTest
    fun setUp() {
        preferences = ReviewPreferencesImpl(FakePreferencesDataStore(emptyPreferences()))
    }

    private companion object {
        const val FIRST_ELIGIBLE_AT = 1_700_000_000_000L
        const val LAST_PROMPTED_AT = 1_700_500_000_000L
        const val VERSION = "2.7.0"
    }
}
