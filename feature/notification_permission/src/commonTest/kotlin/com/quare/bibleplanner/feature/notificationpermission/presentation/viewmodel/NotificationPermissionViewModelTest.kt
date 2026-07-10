package com.quare.bibleplanner.feature.notificationpermission.presentation.viewmodel

import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class NotificationPermissionViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: NotificationPermissionViewModel
    private lateinit var trackedEvents: List<Pair<String, Map<String, Any>>>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        prepareScenario()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `GIVEN the first-time dialog WHEN confirming THEN logs prompted event with is_first_time true`() =
        runTest(testDispatcher) {
            // When
            viewModel.onEvent(NotificationPermissionUiEvent.OnConfirm)

            // Then
            assertEquals(
                listOf("notification_permission_prompted" to mapOf<String, Any>("is_first_time" to true)),
                trackedEvents,
            )
        }

    @Test
    fun `GIVEN a permanently denied dialog WHEN confirming THEN logs no prompted event`() = runTest(testDispatcher) {
        // Given
        viewModel.onEvent(
            NotificationPermissionUiEvent.OnPermissionResult(
                granted = false,
                canAskAgain = false,
            ),
        )

        // When
        viewModel.onEvent(NotificationPermissionUiEvent.OnConfirm)

        // Then
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a confirmed system prompt WHEN the result arrives THEN logs notification_permission_result`() =
        runTest(testDispatcher) {
            // Given
            viewModel.onEvent(NotificationPermissionUiEvent.OnConfirm)

            // When
            viewModel.onEvent(
                NotificationPermissionUiEvent.OnPermissionResult(
                    granted = false,
                    canAskAgain = true,
                ),
            )

            // Then
            assertEquals(
                "notification_permission_result" to mapOf<String, Any>(
                    "is_granted" to false,
                    "can_ask_again" to true,
                ),
                trackedEvents.last(),
            )
        }

    @Test
    fun `GIVEN no system prompt was requested WHEN a synthetic permission result arrives THEN logs no result event`() =
        runTest(testDispatcher) {
            // When
            viewModel.onEvent(
                NotificationPermissionUiEvent.OnPermissionResult(
                    granted = false,
                    canAskAgain = false,
                ),
            )

            // Then
            assertTrue(trackedEvents.isEmpty())
        }

    private fun prepareScenario() {
        val collected = mutableListOf<Pair<String, Map<String, Any>>>()
        trackedEvents = collected
        viewModel = NotificationPermissionViewModel(
            trackEvent = TrackEvent { name, params -> collected += name to params },
        )
    }
}
