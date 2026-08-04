package com.quare.bibleplanner.core.date

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class HasCooldownElapsedUseCase(
    private val currentTimestampProvider: CurrentTimestampProvider,
) {
    operator fun invoke(
        lastOccurredAt: Long?,
        cooldown: Duration,
    ): Boolean {
        if (lastOccurredAt == null) return true
        val elapsed = currentTimestampProvider.getCurrentTimestamp() - lastOccurredAt
        return elapsed.milliseconds >= cooldown
    }
}
