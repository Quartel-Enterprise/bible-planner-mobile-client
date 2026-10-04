package com.quare.bibleplanner.core.inappupdate.domain.usecase.impl

import com.quare.bibleplanner.core.inappupdate.domain.UpdatePromptSource
import com.quare.bibleplanner.core.inappupdate.domain.model.UpdateAvailability
import com.quare.bibleplanner.core.inappupdate.fake.FakeUpdatePromptPreferences
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.InAppUpdateNavRoute
import com.quare.bibleplanner.core.model.route.UpdateDownloadedNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.platform.Platform
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class ShowUpdatePromptUseCaseTest {
    private val navigator = Navigator()
    private val commands = mutableListOf<NavigationCommand>()
    private val preferences = FakeUpdatePromptPreferences(lastPromptedAt = null)
    private var didStartUpdate = false
    private val trackedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
    private lateinit var useCase: ShowUpdatePromptUseCase

    @Test
    fun `GIVEN android WHEN showing the prompt THEN starts the native update flow without navigating`() = runTest {
        // Given
        prepareScenario(platform = Platform.Android)

        // When
        useCase(
            availability = UpdateAvailability.Available(versionName = null),
            source = UpdatePromptSource.STARTUP,
        )

        // Then
        assertTrue(didStartUpdate)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `GIVEN android WHEN showing the prompt THEN tracks the prompt shown event with its source`() = runTest {
        // Given
        prepareScenario(platform = Platform.Android)

        // When
        useCase(
            availability = UpdateAvailability.Available(versionName = null),
            source = UpdatePromptSource.MANUAL,
        )

        // Then
        assertEquals(
            listOf(
                AnalyticsEventNames.UPDATE_PROMPT_SHOWN to
                    mapOf<String, Any>(AnalyticsParams.SOURCE to UpdatePromptSource.MANUAL),
            ),
            trackedEvents,
        )
    }

    @Test
    fun `GIVEN ios WHEN showing the prompt THEN navigates to the update dialog without starting the update`() =
        runTest {
            // Given
            prepareScenario(platform = Platform.Ios)

            // When
            useCase(
                availability = UpdateAvailability.Available(versionName = "2.0.0"),
                source = UpdatePromptSource.STARTUP,
            )

            // Then
            assertEquals(
                listOf<NavigationCommand>(
                    NavigationCommand.Navigate(
                        InAppUpdateNavRoute(versionName = "2.0.0", source = UpdatePromptSource.STARTUP),
                    ),
                ),
                commands,
            )
            assertFalse(didStartUpdate)
        }

    @Test
    fun `GIVEN a downloaded update WHEN showing the prompt THEN opens the restart sheet without starting it`() =
        runTest {
            // Given
            prepareScenario(platform = Platform.Android)

            // When
            useCase(
                availability = UpdateAvailability.Downloaded,
                source = UpdatePromptSource.STARTUP,
            )

            // Then
            assertEquals(listOf<NavigationCommand>(NavigationCommand.Navigate(UpdateDownloadedNavRoute)), commands)
            assertFalse(didStartUpdate)
            assertTrue(trackedEvents.isEmpty())
        }

    @Test
    fun `GIVEN a downloaded update WHEN showing the prompt THEN records it so the cooldown also covers it`() = runTest {
        // Given
        prepareScenario(platform = Platform.Android)

        // When
        useCase(
            availability = UpdateAvailability.Downloaded,
            source = UpdatePromptSource.STARTUP,
        )

        // Then
        assertEquals(NOW, preferences.getLastPromptedAt())
    }

    @Test
    fun `GIVEN a manual check WHEN showing the prompt THEN records it so the cooldown also covers it`() = runTest {
        // Given
        prepareScenario(platform = Platform.Ios)

        // When
        useCase(
            availability = UpdateAvailability.Available(versionName = "2.0.0"),
            source = UpdatePromptSource.MANUAL,
        )

        // Then
        assertEquals(NOW, preferences.getLastPromptedAt())
    }

    private fun TestScope.prepareScenario(platform: Platform) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.commands.collect { commands += it }
        }
        useCase = ShowUpdatePromptUseCase(
            platform = platform,
            startUpdate = { didStartUpdate = true },
            navigator = navigator,
            updatePromptPreferences = preferences,
            currentTimestampProvider = { NOW },
            trackEvent = { name, params -> trackedEvents += name to params },
        )
    }

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}
