package com.quare.bibleplanner.core.plan.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Why: rows are keyed by (user_id, key) and reconciled by updated_at (last-write-wins).
@Serializable
internal data class UserPreferenceDto(
    @SerialName("user_id") val userId: String,
    @SerialName("key") val key: String,
    @SerialName("value") val value: String,
    @SerialName("updated_at") val updatedAt: String,
)
