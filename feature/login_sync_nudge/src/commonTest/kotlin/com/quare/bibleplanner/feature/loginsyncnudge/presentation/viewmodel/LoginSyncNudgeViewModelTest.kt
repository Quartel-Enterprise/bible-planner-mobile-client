package com.quare.bibleplanner.feature.loginsyncnudge.presentation.viewmodel

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.feature.loginsyncnudge.presentation.model.LoginSyncNudgeUiEvent
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

@OptIn(ExperimentalCoroutinesApi::class)
internal class LoginSyncNudgeViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: LoginSyncNudgeViewModel
    private val trackedEvents = mutableListOf<String>()
    private var snoozeCount = 0
    private var dismissPermanentlyCount = 0

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginSyncNudgeViewModel(
            snoozeLoginNudge = { snoozeCount++ },
            dismissLoginNudgePermanently = { dismissPermanentlyCount++ },
            trackEvent = { name, _ -> trackedEvents += name },
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN the checkbox unchecked WHEN tapping login THEN tracks accepted only`() = runTest(testDispatcher) {
        // When
        viewModel.onEvent(LoginSyncNudgeUiEvent.OnLoginClick)

        // Then
        assertEquals(listOf(AnalyticsEventNames.LOGIN_NUDGE_ACCEPTED), trackedEvents)
        assertEquals(0, snoozeCount)
        assertEquals(0, dismissPermanentlyCount)
    }

    @Test
    fun `GIVEN the checkbox checked WHEN tapping login THEN tracks accepted and disabled`() = runTest(testDispatcher) {
        // Given
        viewModel.onEvent(LoginSyncNudgeUiEvent.OnDontShowAgainToggled(isChecked = true))

        // When
        viewModel.onEvent(LoginSyncNudgeUiEvent.OnLoginClick)

        // Then
        assertEquals(
            listOf(
                AnalyticsEventNames.LOGIN_NUDGE_DONT_SHOW_AGAIN_TOGGLED,
                AnalyticsEventNames.LOGIN_NUDGE_ACCEPTED,
                AnalyticsEventNames.LOGIN_NUDGE_DISABLED,
            ),
            trackedEvents,
        )
        assertEquals(1, dismissPermanentlyCount)
    }

    @Test
    fun `GIVEN the checkbox unchecked WHEN tapping not now THEN tracks snoozed`() = runTest(testDispatcher) {
        // When
        viewModel.onEvent(LoginSyncNudgeUiEvent.OnNotNow)

        // Then
        assertEquals(listOf(AnalyticsEventNames.LOGIN_NUDGE_SNOOZED), trackedEvents)
        assertEquals(1, snoozeCount)
    }

    @Test
    fun `GIVEN the checkbox checked WHEN dismissing THEN tracks disabled instead of snoozed`() =
        runTest(testDispatcher) {
            // Given
            viewModel.onEvent(LoginSyncNudgeUiEvent.OnDontShowAgainToggled(isChecked = true))

            // When
            viewModel.onEvent(LoginSyncNudgeUiEvent.OnDismiss)

            // Then
            assertEquals(
                listOf(
                    AnalyticsEventNames.LOGIN_NUDGE_DONT_SHOW_AGAIN_TOGGLED,
                    AnalyticsEventNames.LOGIN_NUDGE_DISABLED,
                ),
                trackedEvents,
            )
            assertEquals(0, snoozeCount)
            assertEquals(1, dismissPermanentlyCount)
        }

    @Test
    fun `GIVEN the checkbox toggled off again WHEN dismissing THEN tracks snoozed`() = runTest(testDispatcher) {
        // Given
        viewModel.onEvent(LoginSyncNudgeUiEvent.OnDontShowAgainToggled(isChecked = true))
        viewModel.onEvent(LoginSyncNudgeUiEvent.OnDontShowAgainToggled(isChecked = false))

        // When
        viewModel.onEvent(LoginSyncNudgeUiEvent.OnDismiss)

        // Then
        assertEquals(
            listOf(
                AnalyticsEventNames.LOGIN_NUDGE_DONT_SHOW_AGAIN_TOGGLED,
                AnalyticsEventNames.LOGIN_NUDGE_DONT_SHOW_AGAIN_TOGGLED,
                AnalyticsEventNames.LOGIN_NUDGE_SNOOZED,
            ),
            trackedEvents,
        )
        assertEquals(1, snoozeCount)
        assertEquals(0, dismissPermanentlyCount)
    }
}
