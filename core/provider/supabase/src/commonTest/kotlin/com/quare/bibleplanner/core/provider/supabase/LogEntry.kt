package com.quare.bibleplanner.core.provider.supabase

import co.touchlab.kermit.Severity

internal data class LogEntry(
    val severity: Severity,
    val tag: String,
    val throwable: Throwable?,
)
