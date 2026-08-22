package com.quare.bibleplanner.feature.dayreadingcomplete.domain.model

sealed interface StudyCtaState {
    data class FreeWithQuota(
        val remaining: Int,
        val limit: Int,
    ) : StudyCtaState

    data class FreeExhausted(
        val limit: Int,
    ) : StudyCtaState

    data object Pro : StudyCtaState
}
