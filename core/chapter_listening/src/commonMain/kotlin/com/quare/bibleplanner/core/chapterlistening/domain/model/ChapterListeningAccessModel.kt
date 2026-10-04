package com.quare.bibleplanner.core.chapterlistening.domain.model

sealed interface ChapterListeningAccessModel {
    data object Open : ChapterListeningAccessModel

    data class UnlockAvailable(
        val rewardedRemainingToday: Int,
    ) : ChapterListeningAccessModel

    data object LimitReached : ChapterListeningAccessModel
}
