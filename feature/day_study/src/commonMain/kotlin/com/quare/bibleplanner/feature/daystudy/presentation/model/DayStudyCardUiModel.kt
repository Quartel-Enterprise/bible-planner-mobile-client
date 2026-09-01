package com.quare.bibleplanner.feature.daystudy.presentation.model

import com.quare.bibleplanner.core.model.loadable.Loadable

data class DayStudyCardUiModel(
    /** `null` when the card has no status worth announcing, which hides the badge. */
    val mode: DayStudyCardMode?,
    val quota: Loadable<DayStudyCardQuotaUiModel>,
    val isPro: Boolean,
)
