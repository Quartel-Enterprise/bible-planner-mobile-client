package com.quare.bibleplanner.feature.daystudy.domain.usecase

import com.quare.bibleplanner.core.clear.domain.ClearDayStudyLocalData
import com.quare.bibleplanner.feature.daystudy.data.datasource.DayStudyLocalDataSource

internal class ClearDayStudyLocalDataUseCase(
    private val localDataSource: DayStudyLocalDataSource,
) : ClearDayStudyLocalData {
    override suspend fun invoke() {
        localDataSource.deleteAll()
    }
}
