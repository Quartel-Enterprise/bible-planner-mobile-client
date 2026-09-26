package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetMaxFreeNotesAmountUseCaseTest {
    private lateinit var remoteConfig: RecordingIntRemoteConfig
    private lateinit var useCase: GetMaxFreeNotesAmountUseCase

    @Test
    fun `GIVEN a remote limit WHEN reading the free notes amount THEN returns it`() = runTest {
        // Given
        prepareScenario(remoteValue = 10)

        // When
        val amount = useCase()

        // Then
        assertEquals(10, amount)
    }

    @Test
    fun `WHEN reading the free notes amount THEN asks for its key with a fallback of three`() = runTest {
        // Given
        prepareScenario(remoteValue = 10)

        // When
        useCase()

        // Then
        assertEquals(listOf("max_free_notes" to 3), remoteConfig.requests)
    }

    private fun prepareScenario(remoteValue: Int) {
        remoteConfig = RecordingIntRemoteConfig(remoteValue)
        useCase = GetMaxFreeNotesAmountUseCase(remoteConfig)
    }
}

private class RecordingIntRemoteConfig(
    private val value: Int,
) : GetIntRemoteConfig {
    val requests = mutableListOf<Pair<String, Int>>()

    override suspend fun invoke(
        key: String,
        default: Int,
    ): Int {
        requests += key to default
        return value
    }
}
