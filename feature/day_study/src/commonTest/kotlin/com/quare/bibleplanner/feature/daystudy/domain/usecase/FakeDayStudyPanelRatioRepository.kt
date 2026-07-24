package com.quare.bibleplanner.feature.daystudy.domain.usecase

import com.quare.bibleplanner.feature.daystudy.domain.repository.DayStudyPanelRatioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeDayStudyPanelRatioRepository(
    initialFraction: Float?,
) : DayStudyPanelRatioRepository {
    private val readingFraction = MutableStateFlow(initialFraction)

    var lastSetFraction: Float? = null
        private set

    override fun observeReadingFraction(): Flow<Float?> = readingFraction

    override suspend fun setReadingFraction(fraction: Float) {
        lastSetFraction = fraction
        readingFraction.value = fraction
    }
}
