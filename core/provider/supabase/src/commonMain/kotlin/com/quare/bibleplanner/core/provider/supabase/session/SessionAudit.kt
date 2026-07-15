package com.quare.bibleplanner.core.provider.supabase.session

internal data class SessionAudit(
    val lastSavedAtMillis: Long?,
    val lastDeletedAtMillis: Long?,
) {
    val isSessionExpected: Boolean =
        lastSavedAtMillis != null && (lastDeletedAtMillis == null || lastSavedAtMillis > lastDeletedAtMillis)
}
