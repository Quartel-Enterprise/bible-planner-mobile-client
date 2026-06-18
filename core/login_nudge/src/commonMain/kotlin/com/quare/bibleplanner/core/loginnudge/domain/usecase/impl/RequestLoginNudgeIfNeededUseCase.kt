package com.quare.bibleplanner.core.loginnudge.domain.usecase.impl

import com.quare.bibleplanner.core.loginnudge.domain.usecase.RequestLoginNudgeIfNeeded
import com.quare.bibleplanner.core.loginnudge.domain.usecase.ShouldShowLoginNudge
import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.LoginSyncNudgeNavRoute

internal class RequestLoginNudgeIfNeededUseCase(
    private val shouldShowLoginNudge: ShouldShowLoginNudge,
    private val navigationEventBus: NavigationEventBus,
) : RequestLoginNudgeIfNeeded {
    override suspend fun invoke() {
        if (shouldShowLoginNudge()) {
            navigationEventBus.send(LoginSyncNudgeNavRoute)
        }
    }
}
