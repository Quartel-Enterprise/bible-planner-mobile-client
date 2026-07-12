package com.quare.bibleplanner.core.devices.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RevokeDeviceRequest(
    @SerialName("device_row_id") val deviceRowId: String,
)
