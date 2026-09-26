package com.quare.bibleplanner.feature.read.presentation.appearance

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.feature.read.domain.model.ReaderFocusAid
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderRulerLines
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ReaderAppearanceViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val storedSettings = ReaderSettingsModel(
        fontSizeSp = 20f,
        font = ReaderFont.GARAMOND,
        isRulerEnabled = true,
        rulerLines = 3,
        isFocusedVerseEnabled = false,
        isVerticalReadingEnabled = true,
    )
    private lateinit var viewModel: ReaderAppearanceViewModel
    private lateinit var settings: MutableSharedFlow<ReaderSettingsModel>
    private lateinit var commands: List<NavigationCommand>
    private lateinit var trackedEvents: List<Pair<String, Map<String, Any>>>
    private lateinit var writes: List<Any>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN settings not loaded yet WHEN observing THEN shows the default appearance`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        val state = viewModel.uiState.value

        // Then
        assertEquals(
            expected = ReaderSettingsModel(
                fontSizeSp = ReaderFontSize.DEFAULT,
                font = ReaderFont.LORA,
                isRulerEnabled = false,
                rulerLines = ReaderRulerLines.DEFAULT,
                isFocusedVerseEnabled = false,
                isVerticalReadingEnabled = false,
            ),
            actual = state.settings,
        )
        assertFalse(state.isFontMenuExpanded)
    }

    @Test
    fun `GIVEN stored settings WHEN they load THEN shows them`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        settings.emit(storedSettings)

        // Then
        assertEquals(
            expected = storedSettings,
            actual = viewModel.uiState.value.settings,
        )
    }

    @Test
    fun `GIVEN the slider being dragged WHEN the size changes THEN saves it without tracking`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(ReaderAppearanceUiEvent.OnFontSizeChange(fontSizeSp = 19f))

            // Then
            assertEquals(
                expected = listOf<Any>(19f),
                actual = writes,
            )
            assertTrue(trackedEvents.isEmpty())
        }

    @Test
    fun `GIVEN the slider WHEN the drag finishes THEN tracks the committed size`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ReaderAppearanceUiEvent.OnFontSizeChangeFinished(fontSizeSp = 21f))

        // Then
        assertEquals(
            expected = listOf("reader_font_size_changed" to mapOf<String, Any>("font_size" to 21f)),
            actual = trackedEvents,
        )
        assertTrue(writes.isEmpty())
    }

    @Test
    fun `GIVEN the font list WHEN picking a font THEN saves and tracks it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ReaderAppearanceUiEvent.OnFontClick(ReaderFont.DYSLEXIC))

        // Then
        assertEquals(
            expected = listOf<Any>(ReaderFont.DYSLEXIC),
            actual = writes,
        )
        assertEquals(
            expected = listOf("reader_font_changed" to mapOf<String, Any>("font" to "dyslexic")),
            actual = trackedEvents,
        )
    }

    @Test
    fun `GIVEN a closed font menu WHEN expanding it THEN shows it expanded and tracks the toggle`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            settings.emit(storedSettings)

            // When
            viewModel.onEvent(ReaderAppearanceUiEvent.OnFontMenuToggle(isExpanded = true))

            // Then
            assertTrue(viewModel.uiState.value.isFontMenuExpanded)
            assertEquals(
                expected = listOf("reader_font_menu_toggled" to mapOf<String, Any>("is_expanded" to true)),
                actual = trackedEvents,
            )
        }

    @Test
    fun `GIVEN the focus aids WHEN picking the ruler THEN saves and tracks it from the sheet`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(ReaderAppearanceUiEvent.OnFocusAidChange(ReaderFocusAid.RULER))

            // Then
            assertEquals(
                expected = listOf<Any>(ReaderFocusAid.RULER),
                actual = writes,
            )
            assertEquals(
                expected = listOf(
                    "reader_focus_aid_changed" to mapOf<String, Any>(
                        "focus_aid" to "ruler",
                        "source" to "appearance_sheet",
                    ),
                ),
                actual = trackedEvents,
            )
        }

    @Test
    fun `GIVEN the ruler WHEN changing its height THEN saves and tracks the line count`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ReaderAppearanceUiEvent.OnRulerLinesChange(lines = 2))

        // Then
        assertEquals(
            expected = listOf<Any>(2),
            actual = writes,
        )
        assertEquals(
            expected = listOf("reader_ruler_height_changed" to mapOf<String, Any>("line_count" to 2)),
            actual = trackedEvents,
        )
    }

    @Test
    fun `GIVEN vertical reading off WHEN turning it on THEN saves and tracks it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ReaderAppearanceUiEvent.OnVerticalReadingChange(isEnabled = true))

        // Then
        assertEquals(
            expected = listOf<Any>(true),
            actual = writes,
        )
        assertEquals(
            expected = listOf("reader_vertical_reading_toggled" to mapOf<String, Any>("is_enabled" to true)),
            actual = trackedEvents,
        )
    }

    @Test
    fun `GIVEN the open sheet WHEN dismissing it THEN goes back and tracks the dismissal`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ReaderAppearanceUiEvent.OnDismiss)

        // Then
        assertEquals(
            expected = listOf<NavigationCommand>(NavigationCommand.NavigateBack),
            actual = commands,
        )
        assertEquals(
            expected = listOf("reader_appearance_dismissed" to emptyMap<String, Any>()),
            actual = trackedEvents,
        )
    }

    private fun TestScope.prepareScenario() {
        val navigator = Navigator()
        val collectedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
        val collectedWrites = mutableListOf<Any>()
        trackedEvents = collectedEvents
        writes = collectedWrites
        settings = MutableSharedFlow(replay = 1)
        viewModel = ReaderAppearanceViewModel(
            setReaderFontSize = { fontSizeSp -> collectedWrites += fontSizeSp },
            setReaderFont = { font -> collectedWrites += font },
            setReaderFocusAid = { focusAid -> collectedWrites += focusAid },
            setReaderRulerLines = { lines -> collectedWrites += lines },
            setReaderVerticalReading = { isEnabled -> collectedWrites += isEnabled },
            navigator = navigator,
            observeReaderSettings = { settings },
            trackEvent = { name, params -> collectedEvents += name to params },
        )
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
    }
}
