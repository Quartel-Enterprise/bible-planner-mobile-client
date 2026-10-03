package com.quare.bibleplanner.core.plan.data.mapper

import com.quare.bibleplanner.core.plan.data.dto.DayMetaDto
import com.quare.bibleplanner.core.provider.room.entity.DayEntity
import kotlin.time.Instant

internal class DayMetaMapper {
    fun toDto(
        userId: String,
        entity: DayEntity,
    ): DayMetaDto = DayMetaDto(
        userId = userId,
        weekNumber = entity.weekNumber,
        dayNumber = entity.dayNumber,
        planType = entity.readingPlanType,
        readTimestamp = entity.readTimestamp,
        notes = entity.notes,
        updatedAt = Instant.fromEpochMilliseconds(entity.metaUpdatedAt ?: 0L).toString(),
    )

    fun toEpochMillis(updatedAt: String): Long = Instant.parse(updatedAt).toEpochMilliseconds()
}
