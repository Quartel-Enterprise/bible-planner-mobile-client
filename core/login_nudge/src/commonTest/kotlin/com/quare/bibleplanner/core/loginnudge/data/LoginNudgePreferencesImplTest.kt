package com.quare.bibleplanner.core.loginnudge.data

import androidx.datastore.preferences.core.emptyPreferences
import com.quare.bibleplanner.core.loginnudge.fake.FakePreferencesDataStore
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class LoginNudgePreferencesImplTest {
    private lateinit var preferences: LoginNudgePreferencesImpl

    @Test
    fun `GIVEN a fresh install WHEN reading the nudge history THEN nothing is recorded`() = runTest {
        // When
        val snoozedAt = preferences.getSnoozedAt()
        val isDontShowAgain = preferences.isDontShowAgain()
        val firstActionAt = preferences.getFirstActionAt()

        // Then
        assertNull(snoozedAt)
        assertFalse(isDontShowAgain)
        assertNull(firstActionAt)
    }

    @Test
    fun `GIVEN a recorded nudge history WHEN reading it back THEN returns each stored value`() = runTest {
        // Given
        preferences.setSnoozedAt(SNOOZED_AT)
        preferences.setDontShowAgain()
        preferences.setFirstActionAt(FIRST_ACTION_AT)

        // When
        val snoozedAt = preferences.getSnoozedAt()
        val isDontShowAgain = preferences.isDontShowAgain()
        val firstActionAt = preferences.getFirstActionAt()

        // Then
        assertEquals(SNOOZED_AT, snoozedAt)
        assertTrue(isDontShowAgain)
        assertEquals(FIRST_ACTION_AT, firstActionAt)
    }

    @BeforeTest
    fun setUp() {
        preferences = LoginNudgePreferencesImpl(FakePreferencesDataStore(emptyPreferences()))
    }

    private companion object {
        const val SNOOZED_AT = 1_700_000_000_000L
        const val FIRST_ACTION_AT = 1_690_000_000_000L
    }
}
