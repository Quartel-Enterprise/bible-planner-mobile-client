package com.quare.bibleplanner.core.chapterlistening.domain.model

sealed interface ChapterListeningAccessModel {
    data object Open : ChapterListeningAccessModel

    data object LoginRequired : ChapterListeningAccessModel

    data class UnlockAvailable(
        val rewardedRemainingToday: Int,
        val isFreeUnlockAvailable: Boolean,
        val allowance: ListeningAllowanceModel,
    ) : ChapterListeningAccessModel

    data class LimitReached(
        val allowance: ListeningAllowanceModel,
    ) : ChapterListeningAccessModel
}
