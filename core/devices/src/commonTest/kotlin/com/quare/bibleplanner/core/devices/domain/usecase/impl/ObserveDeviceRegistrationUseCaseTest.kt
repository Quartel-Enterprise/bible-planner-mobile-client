package com.quare.bibleplanner.core.devices.domain.usecase.impl

import com.quare.bibleplanner.core.devices.fake.FakeDevicesRepository
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveDeviceRegistrationUseCaseTest {
    private lateinit var userId: MutableStateFlow<String?>
    private lateinit var repository: FakeDevicesRepository

    @Test
    fun `GIVEN a signed in user WHEN observing THEN registers this device`() = runTest {
        // Given
        prepareScenario(initialUserId = "user-1")

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = 1,
            actual = repository.registerCalls,
        )
    }

    @Test
    fun `GIVEN no signed in user WHEN observing THEN does not register`() = runTest {
        // Given
        prepareScenario(initialUserId = null)

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = 0,
            actual = repository.registerCalls,
        )
    }

    @Test
    fun `GIVEN a signed in user WHEN another account signs in THEN registers again`() = runTest {
        // Given
        prepareScenario(initialUserId = "user-1")
        runCurrent()

        // When
        userId.value = "user-2"
        runCurrent()

        // Then
        assertEquals(
            expected = 2,
            actual = repository.registerCalls,
        )
    }

    private fun TestScope.prepareScenario(initialUserId: String?) {
        userId = MutableStateFlow(initialUserId)
        repository = FakeDevicesRepository()
        val useCase = ObserveDeviceRegistrationUseCase(
            observeAuthenticatedUserId = ObserveAuthenticatedUserId { userId },
            devicesRepository = repository,
        )
        backgroundScope.launch { useCase() }
    }
}
