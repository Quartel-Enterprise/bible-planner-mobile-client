package com.quare.bibleplanner.core.provider.analytics.domain.usecase

import com.quare.bibleplanner.core.provider.analytics.domain.usecase.impl.TrackEventUseCase
import com.quare.bibleplanner.core.provider.analytics.fake.RecordingAnalyticsService
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TrackEventUseCaseTest {
    private lateinit var analyticsService: RecordingAnalyticsService
    private lateinit var useCase: TrackEventUseCase

    @BeforeTest
    fun setUp() {
        analyticsService = RecordingAnalyticsService(appInstanceId = null)
        useCase = TrackEventUseCase(analyticsService)
    }

    @Test
    fun `GIVEN params of every kind WHEN tracking THEN normalizes them to the types analytics accepts`() {
        // When
        useCase(
            name = "chapter_read",
            params = mapOf(
                "is_first" to true,
                "chapter" to 3,
                "progress" to 0.5f,
                "total" to 10L,
                "book" to "GEN",
            ),
        )

        // Then
        assertEquals(
            expected = listOf(
                "chapter_read" to mapOf<String, Any>(
                    "is_first" to "true",
                    "chapter" to 3L,
                    "progress" to 0.5,
                    "total" to 10L,
                    "book" to "GEN",
                ),
            ),
            actual = analyticsService.loggedEvents,
        )
    }

    @Test
    fun `GIVEN no params WHEN tracking THEN logs the bare event`() {
        // When
        useCase(
            name = "logout",
            params = emptyMap(),
        )

        // Then
        assertEquals(
            expected = listOf("logout" to emptyMap<String, Any>()),
            actual = analyticsService.loggedEvents,
        )
    }
}
