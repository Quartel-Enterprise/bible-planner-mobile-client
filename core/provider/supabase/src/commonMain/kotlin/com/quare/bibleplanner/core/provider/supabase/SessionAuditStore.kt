package com.quare.bibleplanner.core.provider.supabase

internal interface SessionAuditStore {
    suspend fun recordSaved()

    suspend fun recordDeleted()

    suspend fun getAudit(): SessionAudit
}
