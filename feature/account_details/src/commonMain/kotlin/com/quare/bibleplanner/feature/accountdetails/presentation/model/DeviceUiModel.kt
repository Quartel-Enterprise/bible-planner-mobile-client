package com.quare.bibleplanner.feature.accountdetails.presentation.model

import com.quare.bibleplanner.core.date.RelativeTime
import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor

internal data class DeviceUiModel(
    val id: String,
    val name: String,
    val formFactor: DeviceFormFactor,
    val isCurrentDevice: Boolean,
    val locationLine: String?,
    val lastActive: RelativeTime,
    val isSigningOut: Boolean,
)
