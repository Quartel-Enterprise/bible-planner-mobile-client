package com.quare.bibleplanner.core.provider.connectivity.domain.usecase.impl

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class IsConnectedUseCaseTest {
    private lateinit var useCase: IsConnectedUseCase

    @Test
    fun `GIVEN the device is online WHEN checking the connection THEN reports connected`() = runTest {
        // Given
        prepareScenario(connectivity = listOf(true, false))

        // When
        val isConnected = useCase()

        // Then
        assertTrue(isConnected)
    }

    @Test
    fun `GIVEN the device is offline WHEN checking the connection THEN reports disconnected`() = runTest {
        // Given
        prepareScenario(connectivity = listOf(false, true))

        // When
        val isConnected = useCase()

        // Then
        assertFalse(isConnected)
    }

    private fun prepareScenario(connectivity: List<Boolean>) {
        useCase = IsConnectedUseCase { flowOf(*connectivity.toTypedArray()) }
    }
}
