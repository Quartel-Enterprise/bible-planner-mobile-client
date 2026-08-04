package com.quare.bibleplanner.feature.inappupdate.domain.usecase.impl

import com.quare.bibleplanner.core.date.HasCooldownElapsedUseCase
import com.quare.bibleplanner.feature.inappupdate.domain.UpdatePromptPreferences
import com.quare.bibleplanner.feature.inappupdate.domain.UpdatePromptSource
import com.quare.bibleplanner.feature.inappupdate.domain.model.UpdateAvailability
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.CheckForUpdate
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.RequestUpdatePromptIfNeeded
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.ShowUpdatePrompt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

internal class RequestUpdatePromptIfNeededUseCase(
    private val checkForUpdate: CheckForUpdate,
    private val updatePromptPreferences: UpdatePromptPreferences,
    private val hasCooldownElapsed: HasCooldownElapsedUseCase,
    private val showUpdatePrompt: ShowUpdatePrompt,
) : RequestUpdatePromptIfNeeded {
    private val promptCooldown: Duration = 1.hours

    override suspend fun invoke() {
        val hasElapsed = hasCooldownElapsed(
            lastOccurredAt = updatePromptPreferences.getLastPromptedAt(),
            cooldown = promptCooldown,
        )
        if (!hasElapsed) return
        val availability = checkForUpdate()
        if (availability is UpdateAvailability.Available) {
            showUpdatePrompt(
                availability = availability,
                source = UpdatePromptSource.STARTUP,
            )
        }
    }
}
