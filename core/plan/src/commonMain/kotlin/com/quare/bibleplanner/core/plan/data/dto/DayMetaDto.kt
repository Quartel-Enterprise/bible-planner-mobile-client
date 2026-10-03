package com.quare.bibleplanner.core.plan.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Why: the day's read state is deliberately not stored here; it derives from chapter/verse read
// state on each device.
@Serializable
internal data class DayMetaDto(
    @SerialName("user_id") val userId: String,
    @SerialName("week_number") val weekNumber: Int,
    @SerialName("day_number") val dayNumber: Int,
    @SerialName("plan_type") val planType: String,
    @SerialName("read_timestamp") val readTimestamp: Long?,
    @SerialName("notes") val notes: String?,
    @SerialName("updated_at") val updatedAt: String,
)
