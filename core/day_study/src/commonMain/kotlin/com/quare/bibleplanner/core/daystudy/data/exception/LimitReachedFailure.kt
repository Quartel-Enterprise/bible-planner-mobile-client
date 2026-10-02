package com.quare.bibleplanner.core.daystudy.data.exception

import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.sse.SSEClientException

private const val LIMIT_EXCEEDED_STATUS = 402

fun Throwable.isLimitReachedFailure(): Boolean = when (this) {
    is RestException -> statusCode == LIMIT_EXCEEDED_STATUS
    is SSEClientException -> response?.status?.value == LIMIT_EXCEEDED_STATUS
    else -> false
}
