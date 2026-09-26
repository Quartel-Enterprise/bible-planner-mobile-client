package com.quare.bibleplanner.core.daystudy.domain.model

data class DayStudyQuotaModel(
    val freeLimit: Int,
    val remainingFree: Int,
    val isUnlockedForDay: Boolean,
    val hasLocalStudy: Boolean,
)
