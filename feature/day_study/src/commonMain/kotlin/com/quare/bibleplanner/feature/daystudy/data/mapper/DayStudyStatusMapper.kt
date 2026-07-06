package com.quare.bibleplanner.feature.daystudy.data.mapper

import com.quare.bibleplanner.feature.daystudy.data.dto.DayStudyStatusDto
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyStatusModel

internal class DayStudyStatusMapper {
    fun map(dto: DayStudyStatusDto): DayStudyStatusModel = DayStudyStatusModel(
        freeLimit = dto.freeLimit,
        usedCount = dto.usedCount,
        isUnlocked = dto.isUnlocked,
        cacheToken = dto.clientCacheToken,
    )
}
