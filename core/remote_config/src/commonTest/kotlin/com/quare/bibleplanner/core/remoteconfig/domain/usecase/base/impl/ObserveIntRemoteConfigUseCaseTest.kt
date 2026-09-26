package com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl

import com.quare.bibleplanner.core.remoteconfig.fake.FakeRemoteConfigService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ObserveIntRemoteConfigUseCaseTest {
    private lateinit var useCase: ObserveIntRemoteConfigUseCase

    @Test
    fun `GIVEN a remote value WHEN observing the key THEN returns the remote value`() = runTest {
        // Given
        prepareScenario(mapOf(KEY to 7))

        // When
        val value = useCase(
            key = KEY,
            default = 3,
        ).first()

        // Then
        assertEquals(7, value)
    }

    @Test
    fun `GIVEN no remote value WHEN observing the key THEN returns the given default`() = runTest {
        // Given
        prepareScenario(emptyMap())

        // When
        val value = useCase(
            key = KEY,
            default = 3,
        ).first()

        // Then
        assertEquals(3, value)
    }

    @Test
    fun `GIVEN no remote value nor default WHEN observing the key THEN returns zero`() = runTest {
        // Given
        prepareScenario(emptyMap())

        // When
        val value = useCase(KEY).first()

        // Then
        assertEquals(0, value)
    }

    private fun prepareScenario(values: Map<String, Any>) {
        useCase = ObserveIntRemoteConfigUseCase(FakeRemoteConfigService(values))
    }

    private companion object {
        const val KEY = "remote_key"
    }
}
