package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.viewmodel

import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.StudyCtaState

internal fun StudyCtaState.toAnalyticsValue(): String = when (this) {
    is StudyCtaState.FreeWithQuota -> "free"
    is StudyCtaState.FreeExhausted -> "free_exhausted"
    StudyCtaState.Pro -> "pro"
}
