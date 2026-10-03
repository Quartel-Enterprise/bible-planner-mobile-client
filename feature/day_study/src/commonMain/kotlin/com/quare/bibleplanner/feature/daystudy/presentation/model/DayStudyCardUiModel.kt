package com.quare.bibleplanner.feature.daystudy.presentation.model

import com.quare.bibleplanner.core.model.loadable.Loadable

data class DayStudyCardUiModel(
    val mode: DayStudyCardMode?,
    val quota: Loadable<DayStudyCardQuotaUiModel>,
    val isPro: Boolean,
    val isRewardedUnlockOffered: Boolean,
    val rewardedRemainingToday: Int,
)
