package com.quare.bibleplanner.core.devices.domain.usecase.impl

import com.quare.bibleplanner.core.devices.domain.repository.DevicesRepository
import com.quare.bibleplanner.core.devices.domain.usecase.SignOutDevice

internal class SignOutDeviceUseCase(
    private val devicesRepository: DevicesRepository,
) : SignOutDevice {
    override suspend fun invoke(deviceRowId: String): Result<Unit> = devicesRepository.signOutDevice(deviceRowId)
}
