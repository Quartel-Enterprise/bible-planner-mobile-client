package com.quare.bibleplanner.core.devices.domain.model

import kotlin.time.Instant

data class DeviceModel(
    val id: String,
    val deviceId: String,
    val name: String,
    val formFactor: DeviceFormFactor,
    val locationCity: String?,
    val locationCountry: String?,
    val lastActiveAt: Instant,
    val isCurrentDevice: Boolean,
)
