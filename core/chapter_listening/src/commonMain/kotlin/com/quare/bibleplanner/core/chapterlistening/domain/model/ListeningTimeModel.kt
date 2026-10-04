package com.quare.bibleplanner.core.chapterlistening.domain.model

import kotlin.time.Duration

data class ListeningTimeModel(
    val elapsed: Duration,
    val total: Duration,
) {
    val remaining: Duration
        get() = (total - elapsed).coerceAtLeast(Duration.ZERO)
}
