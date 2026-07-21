package com.quare.bibleplanner.core.profile.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ProfileRowDto(
    @SerialName("id") val id: String,
    @SerialName("display_name") val displayName: String?,
    @SerialName("avatar_url") val avatarUrl: String?,
    @SerialName("updated_at") val updatedAt: String,
)
