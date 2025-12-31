package com.quare.bibleplanner.feature.day.data.mapper

import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.provider.room.entity.DayEntity

class DayEntityToModelMapper {
    fun map(
        entity: DayEntity?,
        dayModel: DayModel,
    ): DayModel {
        if (entity == null) {
            return dayModel.copy(
                isRead = false,
                readTimestamp = null,
                notes = null,
            )
        }

        return dayModel.copy(
            isRead = entity.isRead,
            readTimestamp = entity.readTimestamp,
            notes = entity.notes,
        )
    }
}
