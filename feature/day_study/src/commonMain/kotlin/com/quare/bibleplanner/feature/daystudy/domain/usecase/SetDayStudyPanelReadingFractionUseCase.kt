package com.quare.bibleplanner.feature.daystudy.domain.usecase

import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPanelRatio
import com.quare.bibleplanner.feature.daystudy.domain.repository.DayStudyPanelRatioRepository

class SetDayStudyPanelReadingFractionUseCase(
    private val repository: DayStudyPanelRatioRepository,
) {
    suspend operator fun invoke(fraction: Float) = repository.setReadingFraction(
        fraction.coerceIn(
            minimumValue = DayStudyPanelRatio.MIN,
            maximumValue = DayStudyPanelRatio.MAX,
        ),
    )
}
