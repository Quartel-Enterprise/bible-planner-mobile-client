package com.quare.bibleplanner.core.devices.domain.usecase.impl

import com.quare.bibleplanner.core.devices.domain.repository.DevicesRepository
import com.quare.bibleplanner.core.devices.domain.usecase.UnregisterCurrentDevice

internal class UnregisterCurrentDeviceUseCase(
    private val devicesRepository: DevicesRepository,
) : UnregisterCurrentDevice {
    override suspend fun invoke(): Result<Unit> = devicesRepository.unregisterCurrentDevice()
}
