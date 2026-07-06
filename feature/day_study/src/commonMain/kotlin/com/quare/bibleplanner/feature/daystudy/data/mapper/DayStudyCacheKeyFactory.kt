package com.quare.bibleplanner.feature.daystudy.data.mapper

import com.quare.bibleplanner.feature.daystudy.data.dto.DayStudyRequestDto
import com.quare.bibleplanner.feature.daystudy.data.model.DayStudyCacheKey

internal class DayStudyCacheKeyFactory {
    fun create(request: DayStudyRequestDto): DayStudyCacheKey = DayStudyCacheKey(
        passages = request.passages,
        version = request.version,
        language = request.language,
    )
}
