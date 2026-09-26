package com.quare.bibleplanner.feature.donation.pixqr.presentation

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.domain.usecase.GetAppStoreLinkUseCase
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class PixQrViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val defaultLocale: Locale = Locale.getDefault()
    private lateinit var viewModel: PixQrViewModel
    private lateinit var commands: List<NavigationCommand>
    private lateinit var trackedEvents: List<Pair<String, Map<String, Any>>>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        Locale.setDefault(Locale.US)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `GIVEN the pix qr code WHEN sharing it THEN shares a message with the store link`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(PixQrUiEvent.Share)
        val action = viewModel.uiAction.first()

        // Then
        val message = assertIs<PixQrUiAction.ShareQrCode>(action).message
        assertTrue(message.startsWith("🙏 Support Bible Planner with PIX!"))
        assertTrue(message.endsWith("https://play.google.com/store/apps/details?id=com.quare.bibleplanner&hl=en"))
        assertEquals(AnalyticsEventNames.PIX_QR_SHARED to emptyMap(), trackedEvents.single())
    }

    @Test
    fun `GIVEN the pix qr code WHEN dismissing it THEN navigates back`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(PixQrUiEvent.Dismiss)
        runCurrent()

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertEquals(AnalyticsEventNames.PIX_QR_DISMISSED to emptyMap(), trackedEvents.single())
    }

    private fun TestScope.prepareScenario() {
        val navigator = Navigator()
        val recordedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
        trackedEvents = recordedEvents
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
        viewModel = PixQrViewModel(
            getAppStoreLink = GetAppStoreLinkUseCase(
                platform = Platform.Android,
                languageProvider = EnglishLanguageProvider(),
            ),
            navigator = navigator,
            trackEvent = { name, params -> recordedEvents += name to params },
        )
        runCurrent()
    }
}

private class EnglishLanguageProvider : LanguageProvider {
    override fun getDeviceLanguage(): Language = error("unused")

    override fun getAppLanguage(): Language = Language.ENGLISH
}
