package com.quare.bibleplanner.core.inappupdate.data

import androidx.datastore.preferences.core.emptyPreferences
import com.quare.bibleplanner.core.inappupdate.fake.FakePreferencesDataStore
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class UpdatePromptPreferencesImplTest {
    private lateinit var preferences: UpdatePromptPreferencesImpl

    @Test
    fun `GIVEN the prompt was never shown WHEN reading the last prompt time THEN returns null`() = runTest {
        // When
        val lastPromptedAt = preferences.getLastPromptedAt()

        // Then
        assertNull(lastPromptedAt)
    }

    @Test
    fun `GIVEN a stored prompt time WHEN reading the last prompt time THEN returns the stored time`() = runTest {
        // Given
        preferences.setLastPromptedAt(PROMPTED_AT)

        // When
        val lastPromptedAt = preferences.getLastPromptedAt()

        // Then
        assertEquals(PROMPTED_AT, lastPromptedAt)
    }

    @BeforeTest
    fun setUp() {
        preferences = UpdatePromptPreferencesImpl(FakePreferencesDataStore(emptyPreferences()))
    }

    private companion object {
        const val PROMPTED_AT = 1_700_000_000_000L
    }
}
