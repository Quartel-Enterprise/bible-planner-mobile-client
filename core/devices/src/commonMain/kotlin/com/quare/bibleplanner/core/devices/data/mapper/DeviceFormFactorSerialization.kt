package com.quare.bibleplanner.core.devices.data.mapper

import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor

private const val RAW_PHONE = "phone"
private const val RAW_TABLET = "tablet"
private const val RAW_COMPUTER = "computer"
private const val RAW_UNKNOWN = "unknown"

internal fun DeviceFormFactor.toRaw(): String = when (this) {
    DeviceFormFactor.PHONE -> RAW_PHONE
    DeviceFormFactor.TABLET -> RAW_TABLET
    DeviceFormFactor.COMPUTER -> RAW_COMPUTER
    DeviceFormFactor.UNKNOWN -> RAW_UNKNOWN
}

internal fun String.toDeviceFormFactor(): DeviceFormFactor = when (this) {
    RAW_PHONE -> DeviceFormFactor.PHONE
    RAW_TABLET -> DeviceFormFactor.TABLET
    RAW_COMPUTER -> DeviceFormFactor.COMPUTER
    else -> DeviceFormFactor.UNKNOWN
}
