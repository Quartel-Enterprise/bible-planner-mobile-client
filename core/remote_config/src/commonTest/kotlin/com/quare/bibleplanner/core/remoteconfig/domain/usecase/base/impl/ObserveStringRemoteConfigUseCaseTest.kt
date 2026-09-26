package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl

import com.quare.bibleplanner.core.remoteconfig.fake.FakeRemoteConfigService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ObserveStringRemoteConfigUseCaseTest {
    private lateinit var useCase: ObserveStringRemoteConfigUseCase

    @Test
    fun `GIVEN a remote value WHEN observing the key THEN returns the remote value`() = runTest {
        // Given
        prepareScenario(mapOf(KEY to "remote"))

        // When
        val value = useCase(
            key = KEY,
            default = "fallback",
        ).first()

        // Then
        assertEquals("remote", value)
    }

    @Test
    fun `GIVEN no remote value WHEN observing the key THEN returns the given default`() = runTest {
        // Given
        prepareScenario(emptyMap())

        // When
        val value = useCase(
            key = KEY,
            default = "fallback",
        ).first()

        // Then
        assertEquals("fallback", value)
    }

    @Test
    fun `GIVEN no remote value nor default WHEN observing the key THEN returns an empty string`() = runTest {
        // Given
        prepareScenario(emptyMap())

        // When
        val value = useCase(KEY).first()

        // Then
        assertEquals("", value)
    }

    private fun prepareScenario(values: Map<String, Any>) {
        useCase = ObserveStringRemoteConfigUseCase(FakeRemoteConfigService(values))
    }

    private companion object {
        const val KEY = "remote_key"
    }
}
