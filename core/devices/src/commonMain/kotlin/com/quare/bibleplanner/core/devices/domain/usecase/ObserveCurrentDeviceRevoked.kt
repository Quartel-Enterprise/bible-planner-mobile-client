package com.quare.bibleplanner.core.devices.domain.usecase

import kotlinx.coroutines.flow.Flow

/*
 * Why: a remote sign-out deletes this device's row; ending the local session on that
 * makes it take effect immediately while online, not only on the next token refresh.
 */
fun interface ObserveCurrentDeviceRevoked {
    operator fun invoke(): Flow<Unit>
}
