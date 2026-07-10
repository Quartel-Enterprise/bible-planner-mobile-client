package com.quare.bibleplanner.core.loginnudge.domain.usecase.impl

import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.LoginSyncNudgeNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class RequestLoginNudgeIfNeededUseCaseTest {
    private val navigationEventBus = NavigationEventBus()
    private val trackedEvents = mutableListOf<String>()

    @Test
    fun `sends the nudge route when the nudge should be shown`() = runTest {
        val useCase = prepareScenario(shouldShow = true)

        useCase()

        assertEquals(LoginSyncNudgeNavRoute, navigationEventBus.events.first())
    }

    @Test
    fun `tracks the nudge impression when the nudge should be shown`() = runTest {
        val useCase = prepareScenario(shouldShow = true)

        useCase()

        assertEquals(listOf(AnalyticsEventNames.LOGIN_NUDGE_SHOWN), trackedEvents)
    }

    @Test
    fun `sends nothing when the nudge should not be shown`() = runTest {
        val useCase = prepareScenario(shouldShow = false)

        useCase()

        assertNull(navigationEventBus.events.replayCache.firstOrNull())
    }

    @Test
    fun `tracks nothing when the nudge should not be shown`() = runTest {
        val useCase = prepareScenario(shouldShow = false)

        useCase()

        assertTrue(trackedEvents.isEmpty())
    }

    private fun prepareScenario(shouldShow: Boolean): RequestLoginNudgeIfNeededUseCase =
        RequestLoginNudgeIfNeededUseCase(
            shouldShowLoginNudge = { shouldShow },
            navigationEventBus = navigationEventBus,
            trackEvent = { name, _ -> trackedEvents += name },
        )
}
