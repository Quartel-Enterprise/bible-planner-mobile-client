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
    private lateinit var useCase: RequestLoginNudgeIfNeededUseCase

    @Test
    fun `GIVEN the nudge should be shown WHEN requesting it THEN sends the nudge route`() = runTest {
        // Given
        prepareScenario(shouldShow = true)

        // When
        useCase()

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.Navigate(LoginSyncNudgeNavRoute)), commands)
    }

    @Test
    fun `GIVEN the nudge should be shown WHEN requesting it THEN tracks the nudge impression`() = runTest {
        // Given
        prepareScenario(shouldShow = true)

        // When
        useCase()

        // Then
        assertEquals(listOf(AnalyticsEventNames.LOGIN_NUDGE_SHOWN), trackedEvents)
    }

    @Test
    fun `GIVEN the nudge should not be shown WHEN requesting it THEN sends nothing`() = runTest {
        // Given
        prepareScenario(shouldShow = false)

        // When
        useCase()

        // Then
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `GIVEN the nudge should not be shown WHEN requesting it THEN tracks nothing`() = runTest {
        // Given
        prepareScenario(shouldShow = false)

        // When
        useCase()

        // Then
        assertTrue(trackedEvents.isEmpty())
    }

    private fun TestScope.prepareScenario(shouldShow: Boolean) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.commands.collect { commands += it }
        }
        useCase = RequestLoginNudgeIfNeededUseCase(
            shouldShowLoginNudge = { shouldShow },
            navigator = navigator,
            trackEvent = { name, _ -> trackedEvents += name },
        )
    }
}
