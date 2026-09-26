package com.quare.bibleplanner.core.provider.analytics.domain.usecase

import com.quare.bibleplanner.core.provider.analytics.domain.usecase.impl.GetAppInstanceIdUseCase
import com.quare.bibleplanner.core.provider.analytics.fake.RecordingAnalyticsService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAppInstanceIdUseCaseTest {
    @Test
    fun `GIVEN an analytics instance id WHEN reading it THEN returns it`() = runTest {
        // Given
        val useCase = GetAppInstanceIdUseCase(RecordingAnalyticsService(appInstanceId = "instance-1"))

        // When
        val appInstanceId = useCase()

        // Then
        assertEquals(
            expected = "instance-1",
            actual = appInstanceId,
        )
    }
}
