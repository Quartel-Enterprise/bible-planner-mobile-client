package com.quare.bibleplanner.core.chapterstudy.data.mapper

import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyStatusDto
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyStatusModel

internal class ChapterStudyStatusMapper {
    fun map(dto: ChapterStudyStatusDto): ChapterStudyStatusModel = ChapterStudyStatusModel(
        freeLimit = dto.freeLimit,
        usedCount = dto.usedCount,
        isUnlocked = dto.isUnlocked,
        cacheToken = dto.clientCacheToken,
        rewardedRemainingToday = dto.rewardedRemainingToday,
    )
}
