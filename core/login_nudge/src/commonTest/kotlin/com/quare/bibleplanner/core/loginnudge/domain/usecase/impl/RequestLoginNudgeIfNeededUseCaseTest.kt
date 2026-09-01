package com.quare.bibleplanner.core.loginnudge.domain.usecase.impl

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.LoginSyncNudgeNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class RequestLoginNudgeIfNeededUseCaseTest {
    private val navigator = Navigator()
    private val commands = mutableListOf<NavigationCommand>()
    private val trackedEvents = mutableListOf<String>()

    @Test
    fun `sends the nudge route when the nudge should be shown`() = runTest {
        val useCase = prepareScenario(shouldShow = true)

        useCase()

        assertEquals(listOf<NavigationCommand>(NavigationCommand.Navigate(LoginSyncNudgeNavRoute)), commands)
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

        assertTrue(commands.isEmpty())
    }

    @Test
    fun `tracks nothing when the nudge should not be shown`() = runTest {
        val useCase = prepareScenario(shouldShow = false)

        useCase()

        assertTrue(trackedEvents.isEmpty())
    }

    private fun TestScope.prepareScenario(shouldShow: Boolean): RequestLoginNudgeIfNeededUseCase {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.commands.collect { commands += it }
        }
        return RequestLoginNudgeIfNeededUseCase(
            shouldShowLoginNudge = { shouldShow },
            navigator = navigator,
            trackEvent = { name, _ -> trackedEvents += name },
        )
    }
}
