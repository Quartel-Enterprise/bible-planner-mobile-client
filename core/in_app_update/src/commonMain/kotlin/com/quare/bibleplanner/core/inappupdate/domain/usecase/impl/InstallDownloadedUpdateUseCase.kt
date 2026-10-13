package com.quare.bibleplanner.core.inappupdate.domain.usecase.impl

import com.quare.bibleplanner.core.inappupdate.domain.usecase.CompleteUpdateInstall
import com.quare.bibleplanner.core.inappupdate.domain.usecase.InstallDownloadedUpdate
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent

internal class InstallDownloadedUpdateUseCase(
    private val completeUpdateInstall: CompleteUpdateInstall,
    private val trackEvent: TrackEvent,
) : InstallDownloadedUpdate {
    /*
     * Why: a successful install kills the process, so getting here again in the same process means the
     * last attempt didn't go through and this is a retry of the same download, not a new install.
     */
    private var hasTrackedInstall = false

    override suspend fun invoke() {
        if (!hasTrackedInstall) {
            hasTrackedInstall = true
            trackEvent(
                name = AnalyticsEventNames.UPDATE_INSTALL_STARTED,
                params = emptyMap(),
            )
        }
        completeUpdateInstall()
    }
}
