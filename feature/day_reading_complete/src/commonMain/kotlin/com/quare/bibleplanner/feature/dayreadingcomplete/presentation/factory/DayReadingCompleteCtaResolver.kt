package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.factory

import com.quare.bibleplanner.core.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockSurface
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.StudyCtaState
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteCtaDecision
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiState
import software.quare.loadable.valueOrNull

internal class DayReadingCompleteCtaResolver(
    private val generationCoordinator: DayStudyGenerationCoordinator,
    private val trackEvent: TrackEvent,
) {
    // Why: null while the CTA is still loading, when a click has nothing to act on.
    fun resolveClick(
        state: DayReadingCompleteUiState,
        clickedEventName: String,
        source: String,
        generationKey: String,
        requestKey: String,
    ): DayReadingCompleteCtaDecision? {
        val ctaState = (state as? DayReadingCompleteUiState.Loaded)
            ?.ctaState
            ?.valueOrNull()
            ?: return null
        trackEvent(
            name = clickedEventName,
            params = ctaState.toCtaClickedParams(source),
        )
        return resolve(
            ctaState = ctaState,
            generationKey = generationKey,
            requestKey = requestKey,
        )
    }

    private fun resolve(
        ctaState: StudyCtaState,
        generationKey: String,
        requestKey: String,
    ): DayReadingCompleteCtaDecision = when {
        ctaState !is StudyCtaState.FreeExhausted -> DayReadingCompleteCtaDecision.Generate(isRewarded = false)

        generationCoordinator.hasUnservedReward(generationKey) -> DayReadingCompleteCtaDecision.Generate(
            isRewarded = true,
        )

        ctaState.isRewardedUnlockOffered -> DayReadingCompleteCtaDecision.OfferRewardedUnlock(
            StudyUnlockNavRoute(
                surface = StudyUnlockSurface.DAY_READING_COMPLETE,
                paywallSource = PaywallEntrySource.DAY_STUDY,
                requestKey = requestKey,
                rewardedRemainingToday = ctaState.rewardedRemainingToday,
                canUnlockWithoutVideo = false,
            ),
        )

        else -> DayReadingCompleteCtaDecision.OpenPaywall
    }
}
