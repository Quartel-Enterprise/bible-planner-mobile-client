package com.quare.bibleplanner.feature.accountdetails.presentation.mapper

import com.quare.bibleplanner.core.date.toRelativeTime
import com.quare.bibleplanner.core.devices.domain.model.DeviceModel
import com.quare.bibleplanner.feature.accountdetails.presentation.model.DeviceUiModel

internal class DeviceUiModelMapper {
    fun map(model: DeviceModel): DeviceUiModel = DeviceUiModel(
        id = model.id,
        name = model.name,
        formFactor = model.formFactor,
        isCurrentDevice = model.isCurrentDevice,
        locationLine = buildLocationLine(model.locationCity, model.locationCountry),
        lastActive = model.lastActiveAt.toRelativeTime(),
        isSigningOut = false,
    )

    private fun buildLocationLine(
        city: String?,
        country: String?,
    ): String? = listOfNotNull(city, country)
        .filter { it.isNotBlank() }
        .joinToString(separator = ", ")
        .ifBlank { null }
}
