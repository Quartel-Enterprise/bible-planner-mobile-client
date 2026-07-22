package com.quare.bibleplanner.feature.inappupdate.domain.usecase.impl

import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.InAppUpdateNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.inappupdate.domain.UpdatePromptSource
import com.quare.bibleplanner.feature.inappupdate.domain.model.UpdateAvailability
import com.quare.bibleplanner.feature.inappupdate.fake.FakeUpdatePromptPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ShowUpdatePromptUseCaseTest {
    private val navigationEventBus = NavigationEventBus()
    private val preferences = FakeUpdatePromptPreferences(lastPromptedAt = null)
    private var didStartUpdate = false
    private val trackedEvents = mutableListOf<Pair<String, Map<String, Any>>>()

    @Test
    fun `GIVEN android WHEN showing the prompt THEN starts the native update flow without navigating`() = runTest {
        val useCase = prepareScenario(platform = Platform.Android)

        useCase(
            availability = UpdateAvailability.Available(versionName = null),
            source = UpdatePromptSource.STARTUP,
        )

        assertTrue(didStartUpdate)
        assertNull(navigationEventBus.events.replayCache.firstOrNull())
    }

    @Test
    fun `GIVEN android WHEN showing the prompt THEN tracks the prompt shown event with its source`() = runTest {
        val useCase = prepareScenario(platform = Platform.Android)

        useCase(
            availability = UpdateAvailability.Available(versionName = null),
            source = UpdatePromptSource.MANUAL,
        )

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
            val useCase = prepareScenario(platform = Platform.Ios)

            useCase(
                availability = UpdateAvailability.Available(versionName = "2.0.0"),
                source = UpdatePromptSource.STARTUP,
            )

            assertEquals(
                InAppUpdateNavRoute(versionName = "2.0.0", source = UpdatePromptSource.STARTUP),
                navigationEventBus.events.first(),
            )
            assertFalse(didStartUpdate)
        }

    @Test
    fun `GIVEN a manual check WHEN showing the prompt THEN records it so the cooldown also covers it`() = runTest {
        val useCase = prepareScenario(platform = Platform.Ios)

        useCase(
            availability = UpdateAvailability.Available(versionName = "2.0.0"),
            source = UpdatePromptSource.MANUAL,
        )

        assertEquals(NOW, preferences.getLastPromptedAt())
    }

    private fun prepareScenario(platform: Platform): ShowUpdatePromptUseCase = ShowUpdatePromptUseCase(
        platform = platform,
        startUpdate = { didStartUpdate = true },
        navigationEventBus = navigationEventBus,
        updatePromptPreferences = preferences,
        currentTimestampProvider = { NOW },
        trackEvent = { name, params -> trackedEvents += name to params },
    )

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}
