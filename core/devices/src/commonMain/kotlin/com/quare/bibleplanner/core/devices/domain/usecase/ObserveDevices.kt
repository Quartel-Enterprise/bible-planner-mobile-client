package com.quare.bibleplanner.core.devices.domain.usecase

import com.quare.bibleplanner.core.devices.domain.model.DeviceModel
import kotlinx.coroutines.flow.Flow

fun interface ObserveDevices {
    operator fun invoke(): Flow<List<DeviceModel>>
}
