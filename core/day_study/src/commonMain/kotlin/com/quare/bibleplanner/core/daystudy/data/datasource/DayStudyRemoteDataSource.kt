package com.quare.bibleplanner.core.daystudy.data.datasource

import com.quare.bibleplanner.core.daystudy.data.dto.DayStudyRequestDto
import com.quare.bibleplanner.core.daystudy.data.dto.DayStudyStatusDto
import com.quare.bibleplanner.core.daystudy.data.model.DayStudyStreamEvent
import kotlinx.coroutines.flow.Flow

internal interface DayStudyRemoteDataSource {
    fun streamDayStudy(request: DayStudyRequestDto): Flow<DayStudyStreamEvent>

    suspend fun fetchStatus(request: DayStudyRequestDto): Result<DayStudyStatusDto>
}
