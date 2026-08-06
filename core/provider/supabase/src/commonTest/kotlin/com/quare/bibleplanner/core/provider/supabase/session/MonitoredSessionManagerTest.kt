package com.quare.bibleplanner.core.provider.supabase.session

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MonitoredSessionManagerTest {
    private lateinit var delegate: FakeSessionManager
    private lateinit var auditStore: FakeSessionAuditStore
    private lateinit var manager: MonitoredSessionManager
    private lateinit var expectedSessionDeletion: ExpectedSessionDeletion
    private lateinit var logWriter: RecordingLogWriter

    @BeforeTest
    fun setUp() {
        delegate = FakeSessionManager()
        auditStore = FakeSessionAuditStore()
        expectedSessionDeletion = ExpectedSessionDeletion()
        logWriter = RecordingLogWriter()
        Logger.setLogWriters(logWriter)
        manager = MonitoredSessionManager(
            delegate = delegate,
            auditStore = auditStore,
            expectedSessionDeletion = expectedSessionDeletion,
        )
    }

    @AfterTest
    fun tearDown() {
        Logger.setLogWriters(platformLogWriter())
    }

    @Test
    fun `GIVEN an expected deletion WHEN deleting THEN logs it without an exception`() = runTest {
        // Given
        manager.saveSession(session())
        logWriter.entries.clear()

        // When
        expectedSessionDeletion.whileDeleting { manager.deleteSession() }

        // Then
        assertEquals(
            expected = listOf<Pair<Severity, String?>>(Severity.Info to null),
            actual = logWriter.entries,
        )
    }

    @Test
    fun `GIVEN an unexpected deletion WHEN deleting THEN reports the deletion as an error`() = runTest {
        // Given
        manager.saveSession(session())
        logWriter.entries.clear()

        // When
        manager.deleteSession()

        // Then
        assertEquals(
            expected = listOf(Severity.Error to SessionDeletedException::class.simpleName),
            actual = logWriter.entries,
        )
    }

    @Test
    fun `GIVEN the deletion scope has ended WHEN deleting again THEN reports it as an error`() = runTest {
        // Given
        expectedSessionDeletion.whileDeleting { manager.deleteSession() }
        logWriter.entries.clear()

        // When
        manager.deleteSession()

        // Then
        assertEquals(
            expected = listOf(Severity.Error to SessionDeletedException::class.simpleName),
            actual = logWriter.entries,
        )
    }

    @Test
    fun `GIVEN a session WHEN saving THEN delegates and records the save`() = runTest {
        // When
        manager.saveSession(session())

        // Then
        assertEquals(
            expected = listOf("delegate.save", "audit.saved"),
            actual = auditStore.order,
        )
    }

    @Test
    fun `WHEN deleting THEN records the delete before delegating`() = runTest {
        // When
        manager.deleteSession()

        // Then
        assertEquals(
            expected = listOf("audit.deleted", "delegate.delete"),
            actual = auditStore.order,
        )
    }

    @Test
    fun `GIVEN a stored session WHEN loading THEN returns it without touching the audit`() = runTest {
        // Given
        manager.saveSession(session())
        auditStore.order.clear()

        // When
        val loaded = manager.loadSession()

        // Then
        assertEquals(
            expected = "access-token",
            actual = loaded.accessToken,
        )
        assertEquals(
            expected = emptyList(),
            actual = auditStore.order,
        )
    }

    @Test
    fun `GIVEN a save never followed by a delete WHEN the stored session is missing THEN reports the loss once`() =
        runTest {
            // Given
            manager.saveSession(session())
            delegate.stored = null
            auditStore.order.clear()

            // When
            assertFailsWith<IllegalStateException> { manager.loadSession() }

            // Then
            assertEquals(
                expected = listOf("audit.deleted"),
                actual = auditStore.order,
            )
        }

    @Test
    fun `GIVEN a reported loss WHEN loading again THEN does not report it twice`() = runTest {
        // Given
        manager.saveSession(session())
        delegate.stored = null
        assertFailsWith<IllegalStateException> { manager.loadSession() }
        auditStore.order.clear()

        // When
        assertFailsWith<IllegalStateException> { manager.loadSession() }

        // Then
        assertEquals(
            expected = emptyList(),
            actual = auditStore.order,
        )
    }

    @Test
    fun `GIVEN a deleted session WHEN the stored session is missing THEN does not report a loss`() = runTest {
        // Given
        manager.saveSession(session())
        manager.deleteSession()
        auditStore.order.clear()

        // When
        assertFailsWith<IllegalStateException> { manager.loadSession() }

        // Then
        assertEquals(
            expected = emptyList(),
            actual = auditStore.order,
        )
    }

    @Test
    fun `GIVEN a delete whose audit write is not visible yet WHEN loading THEN does not report a loss`() = runTest {
        // Given
        manager.saveSession(session())
        auditStore.ignoreDeletes = true
        manager.deleteSession()
        auditStore.order.clear()

        // When
        assertFailsWith<IllegalStateException> { manager.loadSession() }

        // Then
        assertEquals(
            expected = emptyList(),
            actual = auditStore.order,
        )
    }

    @Test
    fun `GIVEN a delete followed by a new session WHEN the stored session is missing THEN reports the loss`() =
        runTest {
            // Given
            manager.deleteSession()
            manager.saveSession(session())
            delegate.stored = null
            auditStore.order.clear()

            // When
            assertFailsWith<IllegalStateException> { manager.loadSession() }

            // Then
            assertEquals(
                expected = listOf("audit.deleted"),
                actual = auditStore.order,
            )
        }

    @Test
    fun `GIVEN no session ever saved WHEN loading THEN does not report a loss`() = runTest {
        // When
        assertFailsWith<IllegalStateException> { manager.loadSession() }

        // Then
        assertEquals(
            expected = emptyList(),
            actual = auditStore.order,
        )
    }

    private fun session(): UserSession = UserSession(
        accessToken = "access-token",
        refreshToken = "refresh-token",
        expiresIn = 3600,
        tokenType = "bearer",
        user = null,
    )

    private class RecordingLogWriter : LogWriter() {
        val entries = mutableListOf<Pair<Severity, String?>>()

        override fun log(
            severity: Severity,
            message: String,
            tag: String,
            throwable: Throwable?,
        ) {
            entries += severity to throwable?.let { it::class.simpleName }
        }
    }

    private inner class FakeSessionManager : SessionManager {
        var stored: UserSession? = null

        override suspend fun saveSession(session: UserSession) {
            stored = session
            auditStore.order += "delegate.save"
        }

        override suspend fun loadSession(): UserSession = stored ?: error("No entry with the key test")

        override suspend fun deleteSession() {
            stored = null
            auditStore.order += "delegate.delete"
        }
    }

    private class FakeSessionAuditStore : SessionAuditStore {
        val order = mutableListOf<String>()
        var ignoreDeletes = false
        private var lastSavedAt: Long? = null
        private var lastDeletedAt: Long? = null
        private var clock = 0L

        override suspend fun recordSaved() {
            lastSavedAt = ++clock
            order += "audit.saved"
        }

        override suspend fun recordDeleted() {
            if (!ignoreDeletes) lastDeletedAt = ++clock
            order += "audit.deleted"
        }

        override suspend fun getAudit(): SessionAudit = SessionAudit(
            lastSavedAtMillis = lastSavedAt,
            lastDeletedAtMillis = lastDeletedAt,
        )
    }
}
