package com.quare.bibleplanner.core.plan.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote row of the `day_meta` table: user-scoped per-day metadata (read timestamp + notes), keyed by
 * (`user_id`, `week_number`, `day_number`, `plan_type`), reconciled by `updated_at` (Last-Write-Wins).
 *
 * The day's read state itself is not stored here: it derives from chapter/verse read state on each
 * device.
 */
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
