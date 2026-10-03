package com.quare.bibleplanner.core.chapterstudy.domain.model

data class ChapterStudyQuotaModel(
    val freeLimit: Int,
    // Why: studies still generating already count as used, so this is lower than the server count.
    val remainingFree: Int,
    val isUnlocked: Boolean,
    val rewardedRemainingToday: Int,
)
