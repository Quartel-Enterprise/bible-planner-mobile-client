package com.quare.bibleplanner.feature.daystudy.presentation.model

import com.quare.bibleplanner.core.model.loadable.Loadable

internal data class DayStudyCardUiModel(
    val mode: DayStudyCardMode,
    val quota: Loadable<DayStudyCardQuotaUiModel>,
    val isPro: Boolean,
)
