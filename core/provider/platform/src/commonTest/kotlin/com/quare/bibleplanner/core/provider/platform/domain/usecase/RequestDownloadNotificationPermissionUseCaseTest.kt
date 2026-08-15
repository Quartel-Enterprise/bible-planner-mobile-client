package com.quare.bibleplanner.core.provider.platform.domain.usecase

import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.NotificationPermissionNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.platform.notification.NotificationPermissionPromptResult
import com.quare.bibleplanner.core.provider.platform.notification.NotificationPermissionRequester
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class RequestDownloadNotificationPermissionUseCaseTest {
    private val navigationEventBus = NavigationEventBus()
    private val trackedEvents = mutableListOf<Pair<String, Map<String, Any>>>()

    @Test
    fun `tracks nothing and sends nothing when the prompt cannot be shown`() = runTest {
        val useCase = prepareScenario(
            canPrompt = false,
            result = NotificationPermissionPromptResult.GRANTED,
        )

        useCase()

        assertTrue(trackedEvents.isEmpty())
        assertNull(navigationEventBus.events.replayCache.firstOrNull())
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
        assertNull(navigationEventBus.events.replayCache.firstOrNull())
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
        assertNull(navigationEventBus.events.replayCache.firstOrNull())
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
        assertEquals(NotificationPermissionNavRoute, navigationEventBus.events.first())
    }

    private fun prepareScenario(
        canPrompt: Boolean,
        result: NotificationPermissionPromptResult,
    ): RequestDownloadNotificationPermissionUseCase = RequestDownloadNotificationPermissionUseCase(
        notificationPermissionRequester = object : NotificationPermissionRequester {
            override suspend fun canPrompt(): Boolean = canPrompt

            override suspend fun request(): NotificationPermissionPromptResult = result
        },
        trackEvent = { name, params -> trackedEvents += name to params },
        navigationEventBus = navigationEventBus,
    )
}
