package com.quare.bibleplanner.feature.daystudy.domain.model

data class DayStudyQuotaModel(
    val freeLimit: Int,
    val remainingFree: Int,
    val isUnlockedForDay: Boolean,
)
