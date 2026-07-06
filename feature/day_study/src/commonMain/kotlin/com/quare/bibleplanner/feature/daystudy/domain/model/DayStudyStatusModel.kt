package com.quare.bibleplanner.feature.daystudy.domain.model

data class DayStudyStatusModel(
    val freeLimit: Int,
    val usedCount: Int,
    val isUnlocked: Boolean,
    val cacheToken: String,
)
