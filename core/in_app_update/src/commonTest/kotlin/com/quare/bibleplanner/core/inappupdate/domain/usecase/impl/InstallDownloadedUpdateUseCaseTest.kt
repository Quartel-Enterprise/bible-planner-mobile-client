package com.quare.bibleplanner.core.inappupdate.domain.usecase.impl

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class InstallDownloadedUpdateUseCaseTest {
    private val steps = mutableListOf<Step>()
    private val installStarted = Step.Track(
        name = AnalyticsEventNames.UPDATE_INSTALL_STARTED,
        params = emptyMap(),
    )
    private lateinit var useCase: InstallDownloadedUpdateUseCase

    @BeforeTest
    fun setUp() {
        useCase = InstallDownloadedUpdateUseCase(
            completeUpdateInstall = { steps += Step.Complete },
            trackEvent = { name, params -> steps += Step.Track(name = name, params = params) },
        )
    }

    @Test
    fun `GIVEN a downloaded update WHEN installing it THEN tracks the install before completing it`() = runTest {
        // When
        useCase()

        // Then
        assertEquals(listOf(installStarted, Step.Complete), steps)
    }

    @Test
    fun `GIVEN an install that did not go through WHEN installing again THEN retries without tracking twice`() =
        runTest {
            // Given
            useCase()

            // When
            useCase()

            // Then
            assertEquals(listOf(installStarted, Step.Complete, Step.Complete), steps)
        }

    private sealed interface Step {
        data class Track(
            val name: String,
            val params: Map<String, Any>,
        ) : Step

        data object Complete : Step
    }
}
