package com.quare.bibleplanner.feature.daystudy.domain.repository

import kotlinx.coroutines.flow.Flow

interface DayStudyPanelRatioRepository {
    fun observeReadingFraction(): Flow<Float?>

    suspend fun setReadingFraction(fraction: Float)
}
