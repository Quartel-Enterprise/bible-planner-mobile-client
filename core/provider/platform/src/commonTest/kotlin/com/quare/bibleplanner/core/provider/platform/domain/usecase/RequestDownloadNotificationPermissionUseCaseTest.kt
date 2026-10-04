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
    private lateinit var useCase: RequestDownloadNotificationPermissionUseCase

    @Test
    fun `GIVEN the prompt cannot be shown WHEN requesting the permission THEN tracks and sends nothing`() = runTest {
        // Given
        prepareScenario(
            canPrompt = false,
            result = NotificationPermissionPromptResult.GRANTED,
        )

        // When
        useCase()

        // Then
        assertTrue(trackedEvents.isEmpty())
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `GIVEN a showable prompt WHEN requesting the permission THEN tracks the prompted event before the result`() =
        runTest {
            // Given
            prepareScenario(
                canPrompt = true,
                result = NotificationPermissionPromptResult.GRANTED,
            )

            // When
            useCase()

            // Then
            assertEquals(
                listOf(
                    AnalyticsEventNames.NOTIFICATION_PERMISSION_PROMPTED,
                    AnalyticsEventNames.NOTIFICATION_PERMISSION_RESULT,
                ),
                trackedEvents.map { it.first },
            )
        }

    @Test
    fun `GIVEN the user grants the permission WHEN requesting it THEN tracks a granted result without navigating`() =
        runTest {
            // Given
            prepareScenario(
                canPrompt = true,
                result = NotificationPermissionPromptResult.GRANTED,
            )

            // When
            useCase()

            // Then
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
    fun `GIVEN the user denies the permission WHEN requesting it THEN tracks a denied result without navigating`() =
        runTest {
            // Given
            prepareScenario(
                canPrompt = true,
                result = NotificationPermissionPromptResult.DENIED,
            )

            // When
            useCase()

            // Then
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
    fun `GIVEN a permanently denied permission WHEN requesting it THEN tracks it and sends the rationale route`() =
        runTest {
            // Given
            prepareScenario(
                canPrompt = true,
                result = NotificationPermissionPromptResult.PERMANENTLY_DENIED,
            )

            // When
            useCase()

            // Then
            assertEquals(
                mapOf(
                    AnalyticsParams.IS_GRANTED to false,
                    AnalyticsParams.CAN_ASK_AGAIN to false,
                ),
                trackedEvents.last().second,
            )
            assertEquals(
                listOf<NavigationCommand>(NavigationCommand.Navigate(NotificationPermissionNavRoute)),
                commands,
            )
        }

    private fun TestScope.prepareScenario(
        canPrompt: Boolean,
        result: NotificationPermissionPromptResult,
    ) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.commands.collect { commands += it }
        }
        useCase = RequestDownloadNotificationPermissionUseCase(
            notificationPermissionRequester = object : NotificationPermissionRequester {
                override suspend fun canPrompt(): Boolean = canPrompt

                override suspend fun request(): NotificationPermissionPromptResult = result
            },
            trackEvent = { name, params -> trackedEvents += name to params },
            navigator = navigator,
        )
    }
}
