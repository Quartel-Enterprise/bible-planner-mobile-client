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
        if (level == LogLevel.ERROR && throwable.isMissingStoredSession()) {
            logger.d(throwable) { message }
            return
        }
        when (level) {
            LogLevel.DEBUG -> logger.d(throwable) { message }
            LogLevel.INFO -> logger.i(throwable) { message }
            LogLevel.WARNING -> logger.w(throwable) { message }
            LogLevel.ERROR -> logger.e(throwable ?: SupabaseLogException(message)) { message }
            LogLevel.NONE -> Unit
        }
    }

    private fun Throwable?.isMissingStoredSession(): Boolean =
        this is IllegalStateException && message?.startsWith(MISSING_SESSION_MESSAGE_PREFIX) == true

    private companion object {
        const val MISSING_SESSION_MESSAGE_PREFIX = "No entry with the key"
    }
}
