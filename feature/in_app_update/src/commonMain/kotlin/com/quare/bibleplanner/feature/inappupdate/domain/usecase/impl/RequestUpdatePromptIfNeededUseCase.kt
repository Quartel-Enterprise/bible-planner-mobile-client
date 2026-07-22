package com.quare.bibleplanner.feature.inappupdate.domain.usecase.impl

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.feature.inappupdate.domain.UpdatePromptPreferences
import com.quare.bibleplanner.feature.inappupdate.domain.UpdatePromptSource
import com.quare.bibleplanner.feature.inappupdate.domain.model.UpdateAvailability
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.CheckForUpdate
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.RequestUpdatePromptIfNeeded
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.ShowUpdatePrompt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

internal class RequestUpdatePromptIfNeededUseCase(
    private val checkForUpdate: CheckForUpdate,
    private val updatePromptPreferences: UpdatePromptPreferences,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val showUpdatePrompt: ShowUpdatePrompt,
) : RequestUpdatePromptIfNeeded {
    private val promptCooldown: Duration = 1.hours

    override suspend fun invoke() {
        if (!hasCooldownElapsed()) return
        val availability = checkForUpdate()
        if (availability is UpdateAvailability.Available) {
            showUpdatePrompt(
                availability = availability,
                source = UpdatePromptSource.STARTUP,
            )
        }
    }

    private suspend fun hasCooldownElapsed(): Boolean {
        val lastPromptedAt = updatePromptPreferences.getLastPromptedAt() ?: return true
        val elapsed = currentTimestampProvider.getCurrentTimestamp() - lastPromptedAt
        return elapsed.milliseconds >= promptCooldown
    }
}
