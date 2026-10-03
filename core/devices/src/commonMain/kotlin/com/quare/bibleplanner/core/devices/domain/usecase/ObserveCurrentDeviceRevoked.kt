package com.quare.bibleplanner.core.devices.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface ObserveCurrentDeviceRevoked {
    operator fun invoke(): Flow<Unit>
}
