package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl

import com.quare.bibleplanner.core.remoteconfig.fake.FakeRemoteConfigService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetBooleanRemoteConfigUseCaseTest {
    private lateinit var useCase: GetBooleanRemoteConfigUseCase

    @Test
    fun `GIVEN a remote value WHEN reading the key THEN returns the remote value`() = runTest {
        // Given
        prepareScenario(mapOf(KEY to true))

        // When
        val value = useCase(
            key = KEY,
            default = false,
        )

        // Then
        assertEquals(true, value)
    }

    @Test
    fun `GIVEN no remote value WHEN reading the key THEN returns the given default`() = runTest {
        // Given
        prepareScenario(emptyMap())

        // When
        val value = useCase(
            key = KEY,
            default = false,
        )

        // Then
        assertEquals(false, value)
    }

    @Test
    fun `GIVEN no remote value nor default WHEN reading the key THEN returns false`() = runTest {
        // Given
        prepareScenario(emptyMap())

        // When
        val value = useCase(KEY)

        // Then
        assertEquals(false, value)
    }

    private fun prepareScenario(values: Map<String, Any>) {
        useCase = GetBooleanRemoteConfigUseCase(FakeRemoteConfigService(values))
    }

    private companion object {
        const val KEY = "remote_key"
    }
}
