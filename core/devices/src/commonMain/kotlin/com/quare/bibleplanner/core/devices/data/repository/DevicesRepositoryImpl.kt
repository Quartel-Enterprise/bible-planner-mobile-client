package com.quare.bibleplanner.core.devices.data.repository

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.devices.data.DeviceIdProvider
import com.quare.bibleplanner.core.devices.data.DeviceInfoProvider
import com.quare.bibleplanner.core.devices.data.UserDevicesRemoteStore
import com.quare.bibleplanner.core.devices.data.dto.RegisterDeviceRequest
import com.quare.bibleplanner.core.devices.data.local.UserDeviceLocalStore
import com.quare.bibleplanner.core.devices.data.mapper.UserDeviceEntityToDomainMapper
import com.quare.bibleplanner.core.devices.data.mapper.toRaw
import com.quare.bibleplanner.core.devices.domain.model.DeviceModel
import com.quare.bibleplanner.core.devices.domain.repository.DevicesRepository
import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal class DevicesRepositoryImpl(
    private val localStore: UserDeviceLocalStore,
    private val remoteStore: UserDevicesRemoteStore,
    private val deviceIdProvider: DeviceIdProvider,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val entityToDomainMapper: UserDeviceEntityToDomainMapper,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : DevicesRepository {
    override fun observeDevices(): Flow<List<DeviceModel>> = flow {
        val currentDeviceId = deviceIdProvider.getOrCreate()
        emitAll(
            localStore.observeAll().map { entities ->
                entities
                    .map { entity -> entityToDomainMapper.map(entity, currentDeviceId) }
                    .sortedWith(
                        compareByDescending<DeviceModel> { it.isCurrentDevice }
                            .thenByDescending { it.lastActiveAt },
                    )
            },
        )
    }

    // Offline-first: the rename is written locally and marked pending; the sync engine pushes it.
    override suspend fun renameDevice(
        deviceRowId: String,
        name: String,
    ) {
        localStore.renameLocal(
            deviceRowId = deviceRowId,
            name = name,
            now = currentTimestampProvider.getCurrentTimestamp(),
        )
    }

    override suspend fun signOutDevice(deviceRowId: String): Result<Unit> = suspendRunCatching {
        remoteStore.signOutDevice(deviceRowId)
        localStore.deleteById(deviceRowId)
    }

    override suspend fun registerCurrentDevice(): Result<Unit> = suspendRunCatching {
        remoteStore.registerCurrentDevice(
            RegisterDeviceRequest(
                deviceId = deviceIdProvider.getOrCreate(),
                name = deviceInfoProvider.deviceName(),
                platform = deviceInfoProvider.platform(),
                formFactor = deviceInfoProvider.formFactor().toRaw(),
            ),
        )
    }

    override suspend fun unregisterCurrentDevice(): Result<Unit> = suspendRunCatching {
        remoteStore.deleteOwnDevice(deviceIdProvider.getOrCreate())
    }
}
