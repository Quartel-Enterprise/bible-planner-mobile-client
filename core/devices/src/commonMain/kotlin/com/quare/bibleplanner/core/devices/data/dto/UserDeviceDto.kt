package com.quare.bibleplanner.core.devices.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserDeviceDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("name") val name: String,
    @SerialName("platform") val platform: String,
    @SerialName("form_factor") val formFactor: String,
    @SerialName("location_city") val locationCity: String?,
    @SerialName("location_country") val locationCountry: String?,
    @SerialName("last_active_at") val lastActiveAt: String,
    @SerialName("updated_at") val updatedAt: String,
)
