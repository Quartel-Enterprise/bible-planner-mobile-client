package com.quare.bibleplanner.core.provider.supabase

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession

internal class MonitoredSessionManager(
    private val delegate: SessionManager,
    private val auditStore: SessionAuditStore,
) : SessionManager {
    private val logger = Logger.withTag(LOG_TAG)

    override suspend fun saveSession(session: UserSession) {
        delegate.saveSession(session)
        auditStore.recordSaved()
    }

    override suspend fun loadSession(): UserSession = suspendRunCatching { delegate.loadSession() }
        .onFailure { reportIfStorageLoss() }
        .getOrThrow()

    override suspend fun deleteSession() {
        logger.e(SessionDeletedException()) { "Deleting stored Supabase session" }
        auditStore.recordDeleted()
        delegate.deleteSession()
    }

    private suspend fun reportIfStorageLoss() {
        val audit = auditStore.getAudit()
        if (!audit.isSessionExpected) return
        logger.e(
            throwable = SessionStorageLossException(
                lastSavedAtMillis = audit.lastSavedAtMillis,
                lastDeletedAtMillis = audit.lastDeletedAtMillis,
            ),
        ) { "Stored Supabase session vanished without a delete" }
        auditStore.recordDeleted()
    }

    private companion object {
        const val LOG_TAG = "SessionStorage"
    }
}
