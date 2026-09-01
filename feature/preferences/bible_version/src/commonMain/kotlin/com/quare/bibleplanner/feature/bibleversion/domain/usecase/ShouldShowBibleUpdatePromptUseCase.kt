package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.date.HasCooldownElapsedUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.BibleUpdatePromptPreferences
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class ShouldShowBibleUpdatePromptUseCase(
    private val getPendingBibleUpdates: GetPendingBibleUpdatesUseCase,
    private val bibleUpdatePromptPreferences: BibleUpdatePromptPreferences,
    private val hasCooldownElapsed: HasCooldownElapsedUseCase,
) {
    private val promptCooldown: Duration = 4.hours

    suspend operator fun invoke(): Boolean {
        val hasElapsed = hasCooldownElapsed(
            lastOccurredAt = bibleUpdatePromptPreferences.getLastDismissedAt(),
            cooldown = promptCooldown,
        )
        return hasElapsed && getPendingBibleUpdates().isNotEmpty()
    }
}
