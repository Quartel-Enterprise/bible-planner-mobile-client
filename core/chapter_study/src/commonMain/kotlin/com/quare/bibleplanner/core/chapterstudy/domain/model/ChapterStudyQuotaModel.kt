package com.quare.bibleplanner.core.chapterstudy.domain.model

data class ChapterStudyQuotaModel(
    val freeLimit: Int,
    /** Free studies left once the ones still generating are counted as used. */
    val remainingFree: Int,
    /** Whether this chapter's study was already paid for, so generating it spends nothing. */
    val isUnlocked: Boolean,
    val rewardedRemainingToday: Int,
)
