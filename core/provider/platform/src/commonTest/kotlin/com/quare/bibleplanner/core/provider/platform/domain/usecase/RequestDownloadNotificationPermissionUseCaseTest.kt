package com.quare.bibleplanner.core.provider.platform.domain.usecase

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.NotificationPermissionNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.platform.notification.NotificationPermissionPromptResult
import com.quare.bibleplanner.core.provider.platform.notification.NotificationPermissionRequester
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class RequestDownloadNotificationPermissionUseCaseTest {
    private val navigator = Navigator()
    private val commands = mutableListOf<NavigationCommand>()
    private val trackedEvents = mutableListOf<Pair<String, Map<String, Any>>>()

    @Test
    fun `tracks nothing and sends nothing when the prompt cannot be shown`() = runTest {
        val useCase = prepareScenario(
            canPrompt = false,
            result = NotificationPermissionPromptResult.GRANTED,
        )

        useCase()

        assertTrue(trackedEvents.isEmpty())
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `tracks the prompted event before the result event`() = runTest {
        val useCase = prepareScenario(
            canPrompt = true,
            result = NotificationPermissionPromptResult.GRANTED,
        )

        useCase()

        assertEquals(
            listOf(
                AnalyticsEventNames.NOTIFICATION_PERMISSION_PROMPTED,
                AnalyticsEventNames.NOTIFICATION_PERMISSION_RESULT,
            ),
            trackedEvents.map { it.first },
        )
    }

    @Test
    fun `tracks a granted result when the permission is granted`() = runTest {
        val useCase = prepareScenario(
            canPrompt = true,
            result = NotificationPermissionPromptResult.GRANTED,
        )

        useCase()

        assertEquals(
            mapOf(
                AnalyticsParams.IS_GRANTED to true,
                AnalyticsParams.CAN_ASK_AGAIN to true,
            ),
            trackedEvents.last().second,
        )
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `tracks a denied result without navigating when the permission is denied`() = runTest {
        val useCase = prepareScenario(
            canPrompt = true,
            result = NotificationPermissionPromptResult.DENIED,
        )

        useCase()

        assertEquals(
            mapOf(
                AnalyticsParams.IS_GRANTED to false,
                AnalyticsParams.CAN_ASK_AGAIN to true,
            ),
            trackedEvents.last().second,
        )
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `sends the rationale route when the permission is permanently denied`() = runTest {
        val useCase = prepareScenario(
            canPrompt = true,
            result = NotificationPermissionPromptResult.PERMANENTLY_DENIED,
        )

        useCase()

        assertEquals(
            mapOf(
                AnalyticsParams.IS_GRANTED to false,
                AnalyticsParams.CAN_ASK_AGAIN to false,
            ),
            trackedEvents.last().second,
        )
        assertEquals(listOf<NavigationCommand>(NavigationCommand.Navigate(NotificationPermissionNavRoute)), commands)
    }

    private fun TestScope.prepareScenario(
        canPrompt: Boolean,
        result: NotificationPermissionPromptResult,
    ): RequestDownloadNotificationPermissionUseCase {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.commands.collect { commands += it }
        }
        return RequestDownloadNotificationPermissionUseCase(
            notificationPermissionRequester = object : NotificationPermissionRequester {
                override suspend fun canPrompt(): Boolean = canPrompt

                override suspend fun request(): NotificationPermissionPromptResult = result
            },
            trackEvent = { name, params -> trackedEvents += name to params },
            navigator = navigator,
        )
    }
}
