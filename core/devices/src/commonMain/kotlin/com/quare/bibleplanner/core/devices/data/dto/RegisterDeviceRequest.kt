package com.quare.bibleplanner.core.devices.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RegisterDeviceRequest(
    @SerialName("device_id") val deviceId: String,
    @SerialName("name") val name: String,
    @SerialName("platform") val platform: String,
    @SerialName("form_factor") val formFactor: String,
)
