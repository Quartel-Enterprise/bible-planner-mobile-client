package com.quare.bibleplanner.core.chapterlistening.data.exception

import io.github.jan.supabase.exceptions.RestException

private const val UNAUTHORIZED_STATUS = 401

internal fun Throwable.isUnauthorized(): Boolean = when (this) {
    is RestException -> statusCode == UNAUTHORIZED_STATUS
    is ChapterAudioDownloadException -> statusCode == UNAUTHORIZED_STATUS
    else -> false
}
