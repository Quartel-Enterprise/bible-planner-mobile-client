package com.quare.bibleplanner.feature.inappupdate.domain.usecase.impl

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.InAppUpdateNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.isAndroid
import com.quare.bibleplanner.feature.inappupdate.domain.UpdatePromptPreferences
import com.quare.bibleplanner.feature.inappupdate.domain.model.UpdateAvailability
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.ShowUpdatePrompt
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.StartUpdate

internal class ShowUpdatePromptUseCase(
    private val platform: Platform,
    private val startUpdate: StartUpdate,
    private val navigationEventBus: NavigationEventBus,
    private val updatePromptPreferences: UpdatePromptPreferences,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val trackEvent: TrackEvent,
) : ShowUpdatePrompt {
    override suspend fun invoke(
        availability: UpdateAvailability.Available,
        source: String,
    ) {
        updatePromptPreferences.setLastPromptedAt(currentTimestampProvider.getCurrentTimestamp())
        if (platform.isAndroid()) {
            trackEvent(
                name = AnalyticsEventNames.UPDATE_PROMPT_SHOWN,
                params = mapOf(AnalyticsParams.SOURCE to source),
            )
            startUpdate()
        } else {
            navigationEventBus.send(
                InAppUpdateNavRoute(
                    versionName = availability.versionName,
                    source = source,
                ),
            )
        }
    }
}
