package com.quare.bibleplanner.core.inappupdate.domain.usecase.impl

import com.quare.bibleplanner.core.date.HasCooldownElapsedUseCase
import com.quare.bibleplanner.core.inappupdate.domain.UpdatePromptPreferences
import com.quare.bibleplanner.core.inappupdate.domain.UpdatePromptSource
import com.quare.bibleplanner.core.inappupdate.domain.model.UpdateAvailability
import com.quare.bibleplanner.core.inappupdate.domain.usecase.CheckForUpdate
import com.quare.bibleplanner.core.inappupdate.domain.usecase.RequestUpdatePromptIfNeeded
import com.quare.bibleplanner.core.inappupdate.domain.usecase.ShowUpdatePrompt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

internal class RequestUpdatePromptIfNeededUseCase(
    private val checkForUpdate: CheckForUpdate,
    private val updatePromptPreferences: UpdatePromptPreferences,
    private val hasCooldownElapsed: HasCooldownElapsedUseCase,
    private val showUpdatePrompt: ShowUpdatePrompt,
) : RequestUpdatePromptIfNeeded {
    private val availablePromptCooldown: Duration = 1.hours
    private val downloadedPromptCooldown: Duration = 15.minutes

    override suspend fun invoke() {
        val lastPromptedAt = updatePromptPreferences.getLastPromptedAt()
        val hasShortestCooldownElapsed = hasCooldownElapsed(
            lastOccurredAt = lastPromptedAt,
            cooldown = downloadedPromptCooldown,
        )
        if (!hasShortestCooldownElapsed) return
        val availability = checkForUpdate()
        if (availability !is UpdateAvailability.Pending) return
        val hasElapsed = hasCooldownElapsed(
            lastOccurredAt = lastPromptedAt,
            cooldown = availability.getPromptCooldown(),
        )
        if (hasElapsed) {
            showUpdatePrompt(
                availability = availability,
                source = UpdatePromptSource.STARTUP,
            )
        }
    }

    private fun UpdateAvailability.Pending.getPromptCooldown(): Duration = when (this) {
        is UpdateAvailability.Available -> availablePromptCooldown
        UpdateAvailability.Downloaded -> downloadedPromptCooldown
    }
}
