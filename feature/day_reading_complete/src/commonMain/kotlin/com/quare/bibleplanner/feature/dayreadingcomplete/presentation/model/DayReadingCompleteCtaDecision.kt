package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model

import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute

internal sealed interface DayReadingCompleteCtaDecision {
    data class Generate(
        val isRewarded: Boolean,
    ) : DayReadingCompleteCtaDecision

    data class OfferRewardedUnlock(
        val route: StudyUnlockNavRoute,
    ) : DayReadingCompleteCtaDecision

    data object OpenPaywall : DayReadingCompleteCtaDecision
}
