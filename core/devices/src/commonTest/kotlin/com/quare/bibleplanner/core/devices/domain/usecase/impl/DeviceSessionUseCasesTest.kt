package com.quare.bibleplanner.core.devices.domain.usecase.impl

import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor
import com.quare.bibleplanner.core.devices.domain.model.DeviceModel
import com.quare.bibleplanner.core.devices.fake.FakeDevicesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class DeviceSessionUseCasesTest {
    private val failure = IllegalStateException("offline")
    private lateinit var repository: FakeDevicesRepository
    private lateinit var observeDevices: ObserveDevicesUseCase
    private lateinit var signOutDevice: SignOutDeviceUseCase
    private lateinit var unregisterCurrentDevice: UnregisterCurrentDeviceUseCase

    @BeforeTest
    fun setUp() {
        repository = FakeDevicesRepository(
            signOutResult = Result.failure(failure),
            unregisterResult = Result.success(Unit),
        )
        observeDevices = ObserveDevicesUseCase(repository)
        signOutDevice = SignOutDeviceUseCase(repository)
        unregisterCurrentDevice = UnregisterCurrentDeviceUseCase(repository)
    }

    @Test
    fun `GIVEN known devices WHEN observing THEN emits them`() = runTest {
        // Given
        val device = DeviceModel(
            id = "row-1",
            deviceId = "device-1",
            name = "iPhone",
            formFactor = DeviceFormFactor.PHONE,
            locationCity = null,
            locationCountry = null,
            lastActiveAt = Instant.parse("2026-07-11T12:00:00Z"),
            isCurrentDevice = true,
        )
        repository.devices.value = listOf(device)

        // When
        val devices = observeDevices().first()

        // Then
        assertEquals(
            expected = listOf(device),
            actual = devices,
        )
    }

    @Test
    fun `GIVEN a failing revoke WHEN signing a device out THEN returns the failure for that row`() = runTest {
        // When
        val result = signOutDevice("row-1")

        // Then
        assertEquals(
            expected = failure,
            actual = result.exceptionOrNull(),
        )
        assertEquals(
            expected = listOf("row-1"),
            actual = repository.signedOutRowIds,
        )
    }

    @Test
    fun `GIVEN a signed in device WHEN unregistering it THEN asks the repository once`() = runTest {
        // When
        val result = unregisterCurrentDevice()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(
            expected = 1,
            actual = repository.unregisterCalls,
        )
    }
}
