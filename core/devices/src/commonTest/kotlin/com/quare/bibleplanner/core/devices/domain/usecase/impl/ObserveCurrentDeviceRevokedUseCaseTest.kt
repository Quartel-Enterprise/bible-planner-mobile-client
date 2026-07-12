package com.quare.bibleplanner.core.devices.domain.usecase.impl

import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor
import com.quare.bibleplanner.core.devices.domain.model.DeviceModel
import com.quare.bibleplanner.core.devices.domain.usecase.ObserveDevices
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveCurrentDeviceRevokedUseCaseTest {
    private val userId = MutableStateFlow<String?>("user-1")
    private val devices = MutableStateFlow<List<DeviceModel>>(emptyList())
    private val useCase = ObserveCurrentDeviceRevokedUseCase(
        observeAuthenticatedUserId = ObserveAuthenticatedUserId { userId },
        observeDevices = ObserveDevices { devices },
    )

    @Test
    fun `GIVEN the current device was present WHEN it disappears THEN emits once`() = runTest {
        // Given
        val emissions = collect()
        devices.value = listOf(currentDevice())
        runCurrent()

        // When
        devices.value = emptyList()
        runCurrent()

        // Then
        assertEquals(1, emissions.size)
    }

    @Test
    fun `GIVEN the current device was never present WHEN the list is empty THEN never emits`() = runTest {
        // Given
        val emissions = collect()

        // When
        devices.value = emptyList()
        runCurrent()

        // Then
        assertTrue(emissions.isEmpty())
    }

    @Test
    fun `GIVEN no authenticated user WHEN devices change THEN never emits`() = runTest {
        // Given
        userId.value = null
        val emissions = collect()

        // When
        devices.value = listOf(currentDevice())
        runCurrent()
        devices.value = emptyList()
        runCurrent()

        // Then
        assertTrue(emissions.isEmpty())
    }

    private fun kotlinx.coroutines.test.TestScope.collect(): List<Unit> {
        val emissions = mutableListOf<Unit>()
        backgroundScope.launch { useCase().collect { emissions += it } }
        return emissions
    }

    private fun currentDevice() = DeviceModel(
        id = "row-1",
        deviceId = "device-1",
        name = "This phone",
        formFactor = DeviceFormFactor.PHONE,
        locationCity = null,
        locationCountry = null,
        lastActiveAt = Instant.parse("2026-07-11T12:00:00Z"),
        isCurrentDevice = true,
    )
}
