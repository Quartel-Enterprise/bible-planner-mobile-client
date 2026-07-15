package com.quare.bibleplanner.core.provider.supabase

internal class SessionStorageLossException(
    lastSavedAtMillis: Long?,
    lastDeletedAtMillis: Long?,
) : Exception(
        "Stored Supabase session is missing even though the last save was never followed by a delete " +
            "(lastSavedAtMillis=$lastSavedAtMillis, lastDeletedAtMillis=$lastDeletedAtMillis)",
    )
