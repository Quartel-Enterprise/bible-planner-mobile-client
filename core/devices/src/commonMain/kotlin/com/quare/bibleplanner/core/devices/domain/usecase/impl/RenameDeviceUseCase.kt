package com.quare.bibleplanner.core.devices.domain.usecase.impl

import com.quare.bibleplanner.core.devices.domain.repository.DevicesRepository
import com.quare.bibleplanner.core.devices.domain.usecase.RenameDevice
import com.quare.bibleplanner.core.utils.suspendRunCatching

internal class RenameDeviceUseCase(
    private val devicesRepository: DevicesRepository,
) : RenameDevice {
    override suspend fun invoke(
        deviceRowId: String,
        name: String,
    ): Result<Unit> = suspendRunCatching {
        devicesRepository.renameDevice(deviceRowId, name)
    }
}
