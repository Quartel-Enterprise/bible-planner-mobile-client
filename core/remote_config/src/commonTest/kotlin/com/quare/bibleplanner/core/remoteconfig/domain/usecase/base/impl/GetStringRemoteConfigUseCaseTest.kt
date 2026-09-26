package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl

import com.quare.bibleplanner.core.remoteconfig.fake.FakeRemoteConfigService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetStringRemoteConfigUseCaseTest {
    private lateinit var useCase: GetStringRemoteConfigUseCase

    @Test
    fun `GIVEN a remote value WHEN reading the key THEN returns the remote value`() = runTest {
        // Given
        prepareScenario(mapOf(KEY to "remote"))

        // When
        val value = useCase(
            key = KEY,
            default = "fallback",
        )

        // Then
        assertEquals("remote", value)
    }

    @Test
    fun `GIVEN no remote value WHEN reading the key THEN returns the given default`() = runTest {
        // Given
        prepareScenario(emptyMap())

        // When
        val value = useCase(
            key = KEY,
            default = "fallback",
        )

        // Then
        assertEquals("fallback", value)
    }

    private fun prepareScenario(values: Map<String, Any>) {
        useCase = GetStringRemoteConfigUseCase(FakeRemoteConfigService(values))
    }

    private companion object {
        const val KEY = "remote_key"
    }
}
