package com.quare.bibleplanner.core.devices.domain.usecase.impl

import com.quare.bibleplanner.core.devices.domain.usecase.ObserveCurrentDeviceRevoked
import com.quare.bibleplanner.core.devices.domain.usecase.ObserveDevices
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal class ObserveCurrentDeviceRevokedUseCase(
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val observeDevices: ObserveDevices,
) : ObserveCurrentDeviceRevoked {
    // Scoped per session so account switches restart the detection from scratch (never carrying a
    // previous session's "present" state into the next one).
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun invoke(): Flow<Unit> = observeAuthenticatedUserId().flatMapLatest { userId ->
        if (userId == null) {
            emptyFlow()
        } else {
            observeDevices()
                .map { devices -> devices.any { it.isCurrentDevice } }
                .distinctUntilChanged()
                .dropWhile { isPresent -> !isPresent } // wait until this device is registered
                .filter { isPresent -> !isPresent } // then fire when it disappears
                .map { }
        }
    }
}
