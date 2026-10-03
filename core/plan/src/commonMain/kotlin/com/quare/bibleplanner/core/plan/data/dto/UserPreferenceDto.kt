package com.quare.bibleplanner.core.plan.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserPreferenceDto(
    @SerialName("user_id") val userId: String,
    @SerialName("key") val key: String,
    @SerialName("value") val value: String,
    @SerialName("updated_at") val updatedAt: String,
)
