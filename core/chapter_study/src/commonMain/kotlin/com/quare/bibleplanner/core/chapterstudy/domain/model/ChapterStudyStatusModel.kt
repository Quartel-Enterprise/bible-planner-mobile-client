package com.quare.bibleplanner.core.chapterstudy.domain.model

data class ChapterStudyStatusModel(
    val freeLimit: Int,
    val usedCount: Int,
    val isUnlocked: Boolean,
    val cacheToken: String,
    val rewardedRemainingToday: Int,
)
