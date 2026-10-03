package com.quare.bibleplanner.core.chapterstudy.domain.model

data class ChapterStudyQuotaModel(
    val freeLimit: Int,
    val remainingFree: Int,
    val isUnlocked: Boolean,
    val rewardedRemainingToday: Int,
)
