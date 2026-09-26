package com.quare.bibleplanner.core.provider.supabase.session

import androidx.datastore.preferences.core.emptyPreferences
import com.quare.bibleplanner.core.provider.supabase.fake.FakePreferencesDataStore
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DataStoreSessionAuditStoreTest {
    private lateinit var auditStore: DataStoreSessionAuditStore
    private var now = 0L

    @Test
    fun `GIVEN nothing recorded WHEN reading the audit THEN expects no session`() = runTest {
        // When
        val audit = auditStore.getAudit()

        // Then
        assertEquals(
            SessionAudit(
                lastSavedAtMillis = null,
                lastDeletedAtMillis = null,
            ),
            audit,
        )
        assertFalse(audit.isSessionExpected)
    }

    @Test
    fun `GIVEN a saved session WHEN reading the audit THEN expects a stored session`() = runTest {
        // Given
        now = SAVED_AT
        auditStore.recordSaved()

        // When
        val audit = auditStore.getAudit()

        // Then
        assertEquals(SAVED_AT, audit.lastSavedAtMillis)
        assertTrue(audit.isSessionExpected)
    }

    @Test
    fun `GIVEN a session deleted after being saved WHEN reading the audit THEN no longer expects a session`() =
        runTest {
            // Given
            now = SAVED_AT
            auditStore.recordSaved()
            now = DELETED_AT
            auditStore.recordDeleted()

            // When
            val audit = auditStore.getAudit()

            // Then
            assertEquals(
                SessionAudit(
                    lastSavedAtMillis = SAVED_AT,
                    lastDeletedAtMillis = DELETED_AT,
                ),
                audit,
            )
            assertFalse(audit.isSessionExpected)
        }

    @BeforeTest
    fun setUp() {
        now = 0L
        auditStore = DataStoreSessionAuditStore(
            dataStore = FakePreferencesDataStore(emptyPreferences()),
            currentTimestampProvider = { now },
        )
    }

    private companion object {
        const val SAVED_AT = 1_000L
        const val DELETED_AT = 2_000L
    }
}
