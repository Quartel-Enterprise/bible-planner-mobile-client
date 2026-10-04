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
    /*
     * Why: scoped per session via flatMapLatest so an account switch restarts detection
     * and never carries the previous session's "present" state.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun invoke(): Flow<Unit> = observeAuthenticatedUserId().flatMapLatest { userId ->
        if (userId == null) {
            emptyFlow()
        } else {
            observeDevices()
                .map { devices -> devices.any { it.isCurrentDevice } }
                .distinctUntilChanged()
                /*
                 * Why: only a device that was seen registered can be revoked; until then its absence
                 * is not a revocation (a fresh login has no row yet).
                 */
                .dropWhile { isPresent -> !isPresent }
                .filter { isPresent -> !isPresent }
                .map { }
        }
    }
}
