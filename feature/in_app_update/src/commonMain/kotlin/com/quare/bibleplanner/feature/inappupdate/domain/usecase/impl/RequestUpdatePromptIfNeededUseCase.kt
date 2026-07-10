package com.quare.bibleplanner.feature.inappupdate.domain.usecase.impl

import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.InAppUpdateNavRoute
import com.quare.bibleplanner.feature.inappupdate.domain.UpdatePromptSessionGuard
import com.quare.bibleplanner.feature.inappupdate.domain.UpdatePromptSource
import com.quare.bibleplanner.feature.inappupdate.domain.model.UpdateAvailability
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.CheckForUpdate
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.RequestUpdatePromptIfNeeded

internal class RequestUpdatePromptIfNeededUseCase(
    private val checkForUpdate: CheckForUpdate,
    private val sessionGuard: UpdatePromptSessionGuard,
    private val navigationEventBus: NavigationEventBus,
) : RequestUpdatePromptIfNeeded {
    override suspend fun invoke() {
        if (sessionGuard.hasShownThisSession) return
        val availability = checkForUpdate()
        if (availability is UpdateAvailability.Available) {
            sessionGuard.hasShownThisSession = true
            navigationEventBus.send(
                InAppUpdateNavRoute(
                    versionName = availability.versionName,
                    source = UpdatePromptSource.STARTUP,
                ),
            )
        }
    }
}
