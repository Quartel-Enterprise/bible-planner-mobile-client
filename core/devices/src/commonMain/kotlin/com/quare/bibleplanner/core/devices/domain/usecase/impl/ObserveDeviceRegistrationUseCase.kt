package com.quare.bibleplanner.core.devices.domain.usecase.impl

import com.quare.bibleplanner.core.devices.domain.repository.DevicesRepository
import com.quare.bibleplanner.core.devices.domain.usecase.ObserveDeviceRegistration
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import kotlinx.coroutines.flow.collectLatest

internal class ObserveDeviceRegistrationUseCase(
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val devicesRepository: DevicesRepository,
) : ObserveDeviceRegistration {
    override suspend fun invoke() {
        observeAuthenticatedUserId().collectLatest { userId ->
            if (userId != null) devicesRepository.registerCurrentDevice()
        }
    }
}
