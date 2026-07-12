package com.quare.bibleplanner.core.devices.domain.usecase.impl

import com.quare.bibleplanner.core.devices.domain.model.DeviceModel
import com.quare.bibleplanner.core.devices.domain.repository.DevicesRepository
import com.quare.bibleplanner.core.devices.domain.usecase.ObserveDevices
import kotlinx.coroutines.flow.Flow

internal class ObserveDevicesUseCase(
    private val devicesRepository: DevicesRepository,
) : ObserveDevices {
    override fun invoke(): Flow<List<DeviceModel>> = devicesRepository.observeDevices()
}
