package com.quare.bibleplanner.feature.logout.domain.usecase

import kotlin.time.Duration

class FlushTimeoutException(
    flushTimeout: Duration,
) : Exception("Flushing pending changes timed out after $flushTimeout")
