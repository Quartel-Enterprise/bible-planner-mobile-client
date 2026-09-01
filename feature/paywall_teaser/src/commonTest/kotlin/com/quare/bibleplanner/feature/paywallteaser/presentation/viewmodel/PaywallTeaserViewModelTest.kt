package com.quare.bibleplanner.feature.paywallteaser.presentation.viewmodel

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.feature.paywallteaser.presentation.model.PaywallTeaserUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PaywallTeaserViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: PaywallTeaserViewModel
    private val navigator = Navigator()
    private val commands = mutableListOf<NavigationCommand>()
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `subscribing sends the reason to the real paywall`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(PaywallTeaserUiEvent.OnSubscribeClick)
        runCurrent()

        // Then
        assertEquals(
            expected = listOf<NavigationCommand>(
                NavigationCommand.NavigateReplacingTop(
                    PaywallNavRoute(PaywallEntrySource.HIGHLIGHT_CUSTOM_COLOR),
                ),
            ),
            actual = commands,
        )
        assertEquals(
            expected = "paywall_teaser_subscribe_clicked" to mapOf("reason" to "highlight_custom_color"),
            actual = trackedEvents.single(),
        )
    }

    @Test
    fun `dismissing pops the sheet without touching the paywall`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(PaywallTeaserUiEvent.OnDismiss)
        runCurrent()

        // Then
        assertEquals(
            expected = listOf<NavigationCommand>(NavigationCommand.NavigateBack),
            actual = commands,
        )
        assertEquals(
            expected = "paywall_teaser_dismissed" to mapOf("reason" to "highlight_custom_color"),
            actual = trackedEvents.single(),
        )
    }

    private fun TestScope.prepareScenario() {
        trackedEvents = mutableListOf()
        viewModel = PaywallTeaserViewModel(
            route = PaywallTeaserNavRoute(PaywallTeaserReason.HIGHLIGHT_CUSTOM_COLOR),
            navigator = navigator,
            trackEvent = { name, params -> trackedEvents += name to params },
        )
        backgroundScope.launch { navigator.commands.collect { commands += it } }
    }
}
