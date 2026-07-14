package com.quare.bibleplanner.core.provider.supabase

import co.touchlab.kermit.Logger
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.logging.SupabaseLoggingProcessor

internal class KermitSupabaseLoggingProcessor(
    private val minLevel: LogLevel,
) : SupabaseLoggingProcessor {
    override fun isEnabled(level: LogLevel): Boolean = level.ordinal >= minLevel.ordinal

    override fun processLog(
        level: LogLevel,
        tag: String,
        throwable: Throwable?,
        message: String,
    ) {
        val logger = Logger.withTag(tag)
        when (level) {
            LogLevel.DEBUG -> logger.d(throwable) { message }
            LogLevel.INFO -> logger.i(throwable) { message }
            LogLevel.WARNING -> logger.w(throwable) { message }
            LogLevel.ERROR -> logger.e(throwable ?: SupabaseLogException(message)) { message }
            LogLevel.NONE -> Unit
        }
    }
}
