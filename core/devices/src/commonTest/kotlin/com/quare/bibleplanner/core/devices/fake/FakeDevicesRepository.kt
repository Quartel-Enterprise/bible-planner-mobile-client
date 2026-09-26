package com.quare.bibleplanner.core.devices.fake

import com.quare.bibleplanner.core.devices.domain.model.DeviceModel
import com.quare.bibleplanner.core.devices.domain.repository.DevicesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeDevicesRepository(
    private val signOutResult: Result<Unit> = Result.success(Unit),
    private val unregisterResult: Result<Unit> = Result.success(Unit),
    private val renameFailure: Throwable? = null,
) : DevicesRepository {
    val devices = MutableStateFlow<List<DeviceModel>>(emptyList())
    val renames = mutableListOf<Pair<String, String>>()
    val signedOutRowIds = mutableListOf<String>()
    var registerCalls: Int = 0
        private set
    var unregisterCalls: Int = 0
        private set

    override fun observeDevices(): Flow<List<DeviceModel>> = devices

    override suspend fun renameDevice(
        deviceRowId: String,
        name: String,
    ) {
        renameFailure?.let { throw it }
        renames += deviceRowId to name
    }

    override suspend fun signOutDevice(deviceRowId: String): Result<Unit> {
        signedOutRowIds += deviceRowId
        return signOutResult
    }

    override suspend fun registerCurrentDevice(): Result<Unit> {
        registerCalls++
        return Result.success(Unit)
    }

    override suspend fun unregisterCurrentDevice(): Result<Unit> {
        unregisterCalls++
        return unregisterResult
    }
}
