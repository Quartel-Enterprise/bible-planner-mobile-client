package com.quare.bibleplanner.core.daystudy.data.mapper

import com.quare.bibleplanner.core.daystudy.data.dto.DayStudyStatusDto
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyStatusModel

internal class DayStudyStatusMapper {
    fun map(dto: DayStudyStatusDto): DayStudyStatusModel = DayStudyStatusModel(
        freeLimit = dto.freeLimit,
        usedCount = dto.usedCount,
        isUnlocked = dto.isUnlocked,
        cacheToken = dto.clientCacheToken,
    )
}
