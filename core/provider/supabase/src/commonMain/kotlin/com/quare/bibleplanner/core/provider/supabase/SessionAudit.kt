package com.quare.bibleplanner.core.provider.supabase

internal data class SessionAudit(
    val lastSavedAtMillis: Long?,
    val lastDeletedAtMillis: Long?,
) {
    val isSessionExpected: Boolean =
        lastSavedAtMillis != null && (lastDeletedAtMillis == null || lastSavedAtMillis > lastDeletedAtMillis)
}
