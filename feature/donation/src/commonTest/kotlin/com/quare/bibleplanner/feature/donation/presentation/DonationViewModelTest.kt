package com.quare.bibleplanner.feature.donation.presentation

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.PixQrNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.donation.generated.DonationBuildKonfig
import com.quare.bibleplanner.feature.donation.presentation.factory.DonationUiStateFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class DonationViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: DonationViewModel
    private lateinit var actions: List<DonationUiAction>
    private lateinit var commands: List<NavigationCommand>
    private lateinit var trackedEvents: List<Pair<String, Map<String, Any>>>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN every donation address WHEN copying each one THEN copies its configured address`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            DonationType.entries.forEach { type -> viewModel.onEvent(DonationUiEvent.Copy(type)) }

            // Then
            assertEquals(
                listOf(
                    DonationBuildKonfig.BTC_ONCHAIN,
                    DonationBuildKonfig.BTC_LIGHTNING,
                    DonationBuildKonfig.USDT_ERC20,
                    DonationBuildKonfig.USDT_TRC20,
                    DonationBuildKonfig.PIX_KEY,
                ).map(DonationUiAction::Copy),
                actions,
            )
            assertEquals(
                listOf("btc_onchain", "btc_lightning", "usdt_erc20", "usdt_trc20", "pix").map { method ->
                    AnalyticsEventNames.DONATION_METHOD_COPIED to mapOf<String, Any>(AnalyticsParams.METHOD to method)
                },
                trackedEvents,
            )
        }

    @Test
    fun `GIVEN nothing copied WHEN copying the pix key THEN marks it as copied`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(DonationUiEvent.Copy(DonationType.PIX))

        // Then
        assertEquals(DonationType.PIX, viewModel.uiState.value.copiedType)
    }

    @Test
    fun `GIVEN the pix key copied WHEN copying it again THEN clears the copied mark without copying`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            viewModel.onEvent(DonationUiEvent.Copy(DonationType.PIX))

            // When
            viewModel.onEvent(DonationUiEvent.Copy(DonationType.PIX))

            // Then
            assertNull(viewModel.uiState.value.copiedType)
            assertEquals(1, actions.size)
            assertEquals(1, trackedEvents.size)
        }

    @Test
    fun `GIVEN collapsed sections WHEN toggling bitcoin THEN expands it and tracks it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(DonationUiEvent.ToggleBitcoin)

        // Then
        assertTrue(viewModel.uiState.value.isBitcoinExpanded)
        assertEquals(sectionToggled(section = "bitcoin", isExpanded = true), trackedEvents.single())
    }

    @Test
    fun `GIVEN collapsed sections WHEN toggling usdt THEN expands it and tracks it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(DonationUiEvent.ToggleUsdt)

        // Then
        assertTrue(viewModel.uiState.value.isUsdtExpanded)
        assertEquals(sectionToggled(section = "usdt", isExpanded = true), trackedEvents.single())
    }

    @Test
    fun `GIVEN the pix section expanded WHEN toggling pix THEN collapses it and tracks it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        viewModel.onEvent(DonationUiEvent.TogglePix)

        // When
        viewModel.onEvent(DonationUiEvent.TogglePix)

        // Then
        assertEquals(false, viewModel.uiState.value.isPixExpanded)
        assertEquals(sectionToggled(section = "pix", isExpanded = false), trackedEvents.last())
    }

    @Test
    fun `GIVEN the donation sheet WHEN opening github sponsors THEN opens the sponsors page`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(DonationUiEvent.OpenGitHubSponsors)

            // Then
            assertEquals(
                listOf<DonationUiAction>(DonationUiAction.OpenUrl("https://github.com/sponsors/Quartel-Enterprise")),
                actions,
            )
            assertEquals(AnalyticsEventNames.GITHUB_SPONSORS_OPENED to emptyMap(), trackedEvents.single())
        }

    @Test
    fun `GIVEN the donation sheet WHEN opening the pix qr code THEN navigates to it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(DonationUiEvent.OpenPixQr)

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.Navigate(PixQrNavRoute)), commands)
        assertEquals(AnalyticsEventNames.PIX_QR_OPENED to emptyMap(), trackedEvents.single())
    }

    @Test
    fun `GIVEN the donation sheet WHEN dismissing it THEN navigates back`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(DonationUiEvent.Dismiss)

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertEquals(AnalyticsEventNames.DONATION_DISMISSED to emptyMap(), trackedEvents.single())
    }

    private fun sectionToggled(
        section: String,
        isExpanded: Boolean,
    ): Pair<String, Map<String, Any>> = AnalyticsEventNames.DONATION_SECTION_TOGGLED to mapOf(
        AnalyticsParams.SECTION to section,
        AnalyticsParams.IS_EXPANDED to isExpanded,
    )

    private fun TestScope.prepareScenario() {
        val navigator = Navigator()
        val recordedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
        trackedEvents = recordedEvents
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
        viewModel = DonationViewModel(
            navigator = navigator,
            factory = DonationUiStateFactory(EnglishLanguageProvider()),
            trackEvent = { name, params -> recordedEvents += name to params },
        )
        actions = mutableListOf<DonationUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
    }
}

private class EnglishLanguageProvider : LanguageProvider {
    override fun getDeviceLanguage(): Language = Language.ENGLISH

    override fun getAppLanguage(): Language = error("unused")
}
