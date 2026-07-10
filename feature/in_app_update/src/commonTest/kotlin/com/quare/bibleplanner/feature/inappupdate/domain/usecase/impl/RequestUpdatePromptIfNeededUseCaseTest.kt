package com.quare.bibleplanner.feature.inappupdate.domain.usecase.impl

import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.InAppUpdateNavRoute
import com.quare.bibleplanner.feature.inappupdate.domain.UpdatePromptSessionGuard
import com.quare.bibleplanner.feature.inappupdate.domain.model.UpdateAvailability
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class RequestUpdatePromptIfNeededUseCaseTest {
    private val navigationEventBus = NavigationEventBus()

    @Test
    fun `GIVEN an available update WHEN requesting THEN sends the update route with its version`() = runTest {
        val useCase = prepareScenario(availability = UpdateAvailability.Available(versionName = "2.0.0"))

        useCase()

        assertEquals(InAppUpdateNavRoute(versionName = "2.0.0"), navigationEventBus.events.first())
    }

    @Test
    fun `GIVEN an available update WHEN requesting THEN marks the session guard as shown`() = runTest {
        val guard = UpdatePromptSessionGuard()
        val useCase = prepareScenario(
            availability = UpdateAvailability.Available(versionName = "2.0.0"),
            guard = guard,
        )

        useCase()

        assertTrue(guard.hasShownThisSession)
    }

    @Test
    fun `GIVEN no available update WHEN requesting THEN sends nothing`() = runTest {
        val useCase = prepareScenario(availability = UpdateAvailability.NotAvailable)

        useCase()

        assertNull(navigationEventBus.events.replayCache.firstOrNull())
    }

    @Test
    fun `GIVEN the prompt was already shown this session WHEN requesting THEN does not check again`() = runTest {
        val guard = UpdatePromptSessionGuard().apply { hasShownThisSession = true }
        var checked = false
        val useCase = RequestUpdatePromptIfNeededUseCase(
            checkForUpdate = {
                checked = true
                UpdateAvailability.Available(versionName = "2.0.0")
            },
            sessionGuard = guard,
            navigationEventBus = navigationEventBus,
        )

        useCase()

        assertFalse(checked)
        assertNull(navigationEventBus.events.replayCache.firstOrNull())
    }

    private fun prepareScenario(
        availability: UpdateAvailability,
        guard: UpdatePromptSessionGuard = UpdatePromptSessionGuard(),
    ): RequestUpdatePromptIfNeededUseCase = RequestUpdatePromptIfNeededUseCase(
        checkForUpdate = { availability },
        sessionGuard = guard,
        navigationEventBus = navigationEventBus,
    )
}
