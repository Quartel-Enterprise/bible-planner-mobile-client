package com.quare.bibleplanner.feature.daystudy.domain.usecase

import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPanelRatio
import com.quare.bibleplanner.feature.daystudy.domain.repository.DayStudyPanelRatioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveDayStudyPanelReadingFractionUseCase(
    private val repository: DayStudyPanelRatioRepository,
) {
    operator fun invoke(): Flow<Float> = repository.observeReadingFraction().map { fraction ->
        (fraction ?: DayStudyPanelRatio.DEFAULT).coerceIn(
            minimumValue = DayStudyPanelRatio.MIN,
            maximumValue = DayStudyPanelRatio.MAX,
        )
    }
}
