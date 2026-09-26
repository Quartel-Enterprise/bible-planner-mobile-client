package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl

import com.quare.bibleplanner.core.remoteconfig.fake.FakeRemoteConfigService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ObserveBooleanRemoteConfigUseCaseTest {
    private lateinit var useCase: ObserveBooleanRemoteConfigUseCase

    @Test
    fun `GIVEN a remote value WHEN observing the key THEN returns the remote value`() = runTest {
        // Given
        prepareScenario(mapOf(KEY to true))

        // When
        val value = useCase(
            key = KEY,
            default = false,
        ).first()

        // Then
        assertEquals(true, value)
    }

    @Test
    fun `GIVEN no remote value WHEN observing the key THEN returns the given default`() = runTest {
        // Given
        prepareScenario(emptyMap())

        // When
        val value = useCase(
            key = KEY,
            default = false,
        ).first()

        // Then
        assertEquals(false, value)
    }

    private fun prepareScenario(values: Map<String, Any>) {
        useCase = ObserveBooleanRemoteConfigUseCase(FakeRemoteConfigService(values))
    }

    private companion object {
        const val KEY = "remote_key"
    }
}
