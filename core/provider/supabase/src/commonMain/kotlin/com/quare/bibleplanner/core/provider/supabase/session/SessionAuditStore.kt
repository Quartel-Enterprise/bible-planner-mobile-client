package com.quare.bibleplanner.core.provider.supabase.session

internal interface SessionAuditStore {
    suspend fun recordSaved()

    suspend fun recordDeleted()

    suspend fun getAudit(): SessionAudit
}
