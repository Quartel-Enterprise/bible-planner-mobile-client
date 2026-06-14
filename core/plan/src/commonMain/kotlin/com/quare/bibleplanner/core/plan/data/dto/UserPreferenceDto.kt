package com.quare.bibleplanner.core.plan.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote row of the `user_preferences` table: one user-scoped scalar setting, keyed by
 * (`user_id`, `key`), reconciled by `updated_at` (Last-Write-Wins).
 */
@Serializable
internal data class UserPreferenceDto(
    @SerialName("user_id") val userId: String,
    @SerialName("key") val key: String,
    @SerialName("value") val value: String,
    @SerialName("updated_at") val updatedAt: String,
)
