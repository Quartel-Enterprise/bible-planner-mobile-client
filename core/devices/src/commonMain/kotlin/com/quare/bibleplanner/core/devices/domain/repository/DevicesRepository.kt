package com.quare.bibleplanner.core.devices.domain.repository

import com.quare.bibleplanner.core.devices.domain.model.DeviceModel
import kotlinx.coroutines.flow.Flow

interface DevicesRepository {
    fun observeDevices(): Flow<List<DeviceModel>>

    suspend fun renameDevice(
        deviceRowId: String,
        name: String,
    )

    suspend fun signOutDevice(deviceRowId: String): Result<Unit>

    suspend fun registerCurrentDevice(): Result<Unit>

    suspend fun unregisterCurrentDevice(): Result<Unit>
}
