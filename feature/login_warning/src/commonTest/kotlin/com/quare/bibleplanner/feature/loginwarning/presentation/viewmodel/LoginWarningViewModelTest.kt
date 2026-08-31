package com.quare.bibleplanner.feature.loginwarning.presentation.viewmodel

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.LoginNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.loginwarning.presentation.model.LoginWarningUiEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class LoginWarningViewModelTest {
    private val navigator = Navigator()
    private val commands = mutableListOf<NavigationCommand>()
    private val trackedEvents = mutableListOf<Pair<String, Map<String, Any>>>()

    private lateinit var viewModel: LoginWarningViewModel

    @Test
    fun `GIVEN the warning dialog WHEN accepting the login THEN replaces the dialog with the login screen`() = runTest {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(LoginWarningUiEvent.OnLoginClick)

        // Then
        assertEquals(
            listOf<NavigationCommand>(
                NavigationCommand.NavigateReplacingTop(LoginNavRoute(notifyResultViaSnackbar = true)),
            ),
            commands,
        )
    }

    @Test
    fun `GIVEN the warning dialog WHEN dismissing it THEN navigates back`() = runTest {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(LoginWarningUiEvent.OnDismiss)

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
    }

    @Test
    fun `GIVEN the warning dialog WHEN accepting the login THEN tracks the acceptance with its reason`() = runTest {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(LoginWarningUiEvent.OnLoginClick)

        // Then
        assertEquals(
            listOf(
                AnalyticsEventNames.LOGIN_WARNING_SHOWN to reasonParams,
                AnalyticsEventNames.LOGIN_WARNING_ACCEPTED to reasonParams,
            ),
            trackedEvents,
        )
    }

    private val reasonParams: Map<String, Any>
        get() = mapOf(AnalyticsParams.REASON to LoginWarningReason.Purchase.key)

    private fun TestScope.prepareScenario() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.commands.collect { commands += it }
        }
        viewModel = LoginWarningViewModel(
            route = LoginWarningNavRoute(reason = LoginWarningReason.Purchase.key),
            trackEvent = TrackEvent { name, params -> trackedEvents += name to params },
            navigator = navigator,
        )
    }
}
