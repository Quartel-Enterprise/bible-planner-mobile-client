package com.quare.bibleplanner.feature.verse.selectionmenu.presentation

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.quare.bibleplanner.core.books.domain.model.VersesShareContentModel
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningType
import com.quare.bibleplanner.core.model.route.DeleteHighlightColorNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.core.model.route.VerseNoteNavRoute
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.verseannotations.domain.model.ChapterAnnotations
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.PresetHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.SelectionNoteUiModel
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiAction
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class VerseSelectionViewModelTest {
    private val testChapter = ChapterRef(
        bibleVersionId = "ACF",
        bookId = BookId.GEN,
        chapterNumber = 3,
    )

    private val testDispatcher = UnconfinedTestDispatcher()
    private val yellow = HighlightColor.Preset(PresetHighlightColor.YELLOW)
    private lateinit var viewModel: VerseSelectionViewModel
    private lateinit var viewModelStore: ViewModelStore
    private lateinit var actions: List<VerseSelectionUiAction>
    private val navigator = Navigator()
    private lateinit var commands: List<NavigationCommand>
    private lateinit var selection: MutableStateFlow<VerseSelection?>
    private lateinit var appliedHighlights: MutableList<Pair<List<VerseRef>, HighlightColor>>
    private lateinit var clearedCount: MutableList<Unit>
    private lateinit var trackedEvents: MutableList<String>
    private lateinit var trackedParams: MutableMap<String, Map<String, Any>>
    private lateinit var addedCustomColors: MutableList<HighlightColor.Custom>
    private lateinit var toggledSavedRefs: MutableList<List<VerseRef>>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        viewModelStore.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a selection made by the reader WHEN the state settles THEN describes the selected verses`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(1, 2),
                actual = viewModel.uiState.value?.verseNumbers,
            )
        }

    @Test
    fun `GIVEN a selection WHEN the reader grows it THEN follows the new verses`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        selection.value = verseSelection(listOf(1, 2, 3))
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(1, 2, 3),
            actual = viewModel.uiState.value?.verseNumbers,
        )
    }

    @Test
    fun `GIVEN a selection WHEN the reader empties it THEN has nothing to show`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        selection.value = null
        runCurrent()

        // Then
        assertNull(viewModel.uiState.value)
    }

    @Test
    fun `GIVEN a selection WHEN tapping a highlight color THEN applies it to the whole selection`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(VerseSelectionUiEvent.OnHighlightColorClick(yellow))
            runCurrent()

            // Then
            val (refs, color) = appliedHighlights.single()
            assertEquals(
                expected = listOf(1, 2),
                actual = refs.map { it.verseNumber },
            )
            assertEquals(
                expected = yellow,
                actual = color,
            )
            assertTrue(trackedEvents.contains("verse_highlight_applied"))
        }

    @Test
    fun `GIVEN a pro user WHEN opening the custom color picker THEN opens it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isPro = true)

        // When
        viewModel.onEvent(VerseSelectionUiEvent.OnCustomColorPickerOpen)
        runCurrent()

        // Then
        assertTrue(viewModel.uiState.value?.customColorPicker != null)
        assertTrue(actions.isEmpty())
        assertTrue(commands.isEmpty())
        assertTrue(trackedEvents.contains("highlight_color_picker_opened"))
    }

    @Test
    fun `GIVEN a free user WHEN opening the custom color picker THEN sends them to the paywall instead`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = false)

            // When
            viewModel.onEvent(VerseSelectionUiEvent.OnCustomColorPickerOpen)
            runCurrent()

            // Then
            assertNull(viewModel.uiState.value?.customColorPicker)
            assertEquals(
                expected = NavigationCommand.Navigate(
                    PaywallTeaserNavRoute(PaywallTeaserReason.HIGHLIGHT_CUSTOM_COLOR),
                ),
                actual = commands.single(),
            )
            assertTrue(trackedEvents.contains("highlight_custom_color_locked_clicked"))
        }

    @Test
    fun `GIVEN a free user WHEN tapping a locked preset THEN sends them to the paywall`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isPro = false)

        // When
        viewModel.onEvent(VerseSelectionUiEvent.OnLockedColorClick)
        runCurrent()

        // Then
        assertTrue(appliedHighlights.isEmpty())
        assertEquals(
            expected = NavigationCommand.Navigate(
                PaywallTeaserNavRoute(PaywallTeaserReason.HIGHLIGHT_CUSTOM_COLOR),
            ),
            actual = commands.single(),
        )
        assertTrue(trackedEvents.contains("highlight_custom_color_locked_clicked"))
    }

    @Test
    fun `GIVEN a selection WHEN closing the menu THEN clears the selection and pops itself`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(VerseSelectionUiEvent.OnClearSelectionClick)
            runCurrent()

            // Then
            assertEquals(
                expected = 1,
                actual = clearedCount.size,
            )
            assertEquals(
                expected = NavigationCommand.NavigateBack,
                actual = commands.single(),
            )
        }

    @Test
    fun `GIVEN a selection WHEN the reader empties it THEN does not navigate`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        selection.value = null
        runCurrent()

        // Then
        assertTrue(actions.isEmpty())
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `GIVEN a selection of several verses WHEN the reader deselects them one by one THEN never navigates back`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            selection.value = verseSelection(listOf(1, 2, 3))
            runCurrent()

            // When
            selection.value = verseSelection(listOf(1, 3))
            runCurrent()
            selection.value = verseSelection(listOf(3))
            runCurrent()
            selection.value = null
            runCurrent()

            // Then
            assertNull(viewModel.uiState.value)
            assertTrue(commands.none { it == NavigationCommand.NavigateBack })
            assertTrue(commands.isEmpty())
            assertTrue(actions.isEmpty())
        }

    @Test
    fun `GIVEN an open selection menu WHEN system back drops it THEN clears the selection without navigating`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            runCurrent()

            // When
            viewModelStore.clear()
            runCurrent()

            // Then
            assertEquals(
                expected = 1,
                actual = clearedCount.size,
            )
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN the close button was tapped WHEN the entry is dropped THEN clears again without navigating twice`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            viewModel.onEvent(VerseSelectionUiEvent.OnClearSelectionClick)
            runCurrent()

            // When
            viewModelStore.clear()
            runCurrent()

            // Then
            assertEquals(
                expected = 2,
                actual = clearedCount.size,
            )
            assertEquals(
                expected = listOf<NavigationCommand>(NavigationCommand.NavigateBack),
                actual = commands,
            )
        }

    @Test
    fun `GIVEN a selection WHEN copying it THEN copies the passage as it is shared`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(VerseSelectionUiEvent.OnCopyClick)
        runCurrent()

        // Then
        assertEquals(
            expected = VerseSelectionUiAction.CopyToClipboard("Gênesis 3:1-2 ARC\nVerse text"),
            actual = actions.first(),
        )
    }

    @Test
    fun `GIVEN a selection WHEN tapping note THEN opens the note editor for the selected passage`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(VerseSelectionUiEvent.OnNoteClick)
            runCurrent()

            // Then
            assertEquals(
                expected = NavigationCommand.Navigate(
                    VerseNoteNavRoute(
                        bibleVersionId = testChapter.bibleVersionId,
                        bookId = testChapter.bookId.name,
                        chapterNumber = testChapter.chapterNumber,
                        verseNumbers = listOf(1, 2),
                        noteId = null,
                    ),
                ),
                actual = commands.single(),
            )
        }

    @Test
    fun `GIVEN a free user at the verse notes limit WHEN tapping note THEN sends them to the free warning`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isAddVerseNoteBlocked = true)

            // When
            viewModel.onEvent(VerseSelectionUiEvent.OnNoteClick)
            runCurrent()

            // Then
            assertEquals(
                expected = NavigationCommand.Navigate(
                    AddNotesFreeWarningNavRoute(
                        maxFreeNotesAmount = MAX_FREE_VERSE_NOTES,
                        type = AddNotesFreeWarningType.VERSE,
                    ),
                ),
                actual = commands.single(),
            )
            assertEquals(
                expected = mapOf<String, Any>(
                    "max_free_notes" to MAX_FREE_VERSE_NOTES,
                    "source" to "selection_menu",
                ),
                actual = trackedParams["verse_notes_limit_reached"],
            )
        }

    @Test
    fun `GIVEN a selection touching a note at the verse notes limit WHEN tapping note THEN opens that note`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                isAddVerseNoteBlocked = true,
                noteIdByVerse = mapOf(
                    1 to "note-1",
                    4 to "note-1",
                ),
            )

            // When
            viewModel.onEvent(VerseSelectionUiEvent.OnNoteClick)
            runCurrent()

            // Then
            assertEquals(
                expected = NavigationCommand.Navigate(
                    VerseNoteNavRoute(
                        bibleVersionId = testChapter.bibleVersionId,
                        bookId = testChapter.bookId.name,
                        chapterNumber = testChapter.chapterNumber,
                        verseNumbers = listOf(1, 4),
                        noteId = "note-1",
                    ),
                ),
                actual = commands.single(),
            )
        }

    @Test
    fun `GIVEN a selection touching a note WHEN the state settles THEN exposes that note`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            noteIdByVerse = mapOf(
                2 to "note-1",
                3 to "note-1",
            ),
        )

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = SelectionNoteUiModel(
                noteId = "note-1",
                verseNumbers = listOf(2, 3),
            ),
            actual = viewModel.uiState.value?.note,
        )
    }

    @Test
    fun `GIVEN a selection WHEN tapping share THEN opens sharing for the selected passage`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(VerseSelectionUiEvent.OnShareClick)
        runCurrent()

        // Then
        assertEquals(
            expected = NavigationCommand.Navigate(
                ShareVerseNavRoute(
                    bookId = BookId.GEN.name,
                    chapterNumber = 3,
                    verseNumbers = listOf(1, 2),
                ),
            ),
            actual = commands.single(),
        )
    }

    @Test
    fun `GIVEN the color already on the selection WHEN tapping it THEN removes the highlight`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isColorApplied = false)

            // When
            viewModel.onEvent(VerseSelectionUiEvent.OnHighlightColorClick(yellow))
            runCurrent()

            // Then
            assertTrue(trackedEvents.contains("verse_highlight_removed"))
        }

    @Test
    fun `GIVEN an edited custom color WHEN applying it THEN saves it and applies it to the selection`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = true)
            viewModel.onEvent(VerseSelectionUiEvent.OnCustomColorPickerOpen)
            viewModel.onEvent(
                VerseSelectionUiEvent.OnCustomColorChange(
                    hue = 120,
                    lightness = 60,
                ),
            )

            // When
            viewModel.onEvent(VerseSelectionUiEvent.OnCustomColorApplyClick)
            runCurrent()

            // Then
            val customColor = HighlightColor.Custom(
                hue = 120,
                lightness = 60,
            )
            assertEquals(
                expected = listOf(customColor),
                actual = addedCustomColors,
            )
            assertEquals(
                expected = customColor,
                actual = appliedHighlights.single().second,
            )
            assertNull(viewModel.uiState.value?.customColorPicker)
            assertTrue(trackedEvents.contains("highlight_custom_color_created"))
        }

    @Test
    fun `GIVEN an open custom color picker WHEN cancelling it THEN closes it without saving`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = true)
            viewModel.onEvent(VerseSelectionUiEvent.OnCustomColorPickerOpen)

            // When
            viewModel.onEvent(VerseSelectionUiEvent.OnCustomColorCancelClick)
            runCurrent()

            // Then
            assertNull(viewModel.uiState.value?.customColorPicker)
            assertTrue(addedCustomColors.isEmpty())
        }

    @Test
    fun `GIVEN a custom color WHEN long pressing it THEN opens its delete confirmation`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        val customColor = HighlightColor.Custom(
            hue = 200,
            lightness = 40,
        )

        // When
        viewModel.onEvent(VerseSelectionUiEvent.OnCustomColorLongClick(customColor))

        // Then
        assertEquals(
            expected = NavigationCommand.Navigate(DeleteHighlightColorNavRoute(colorKey = customColor.key)),
            actual = commands.single(),
        )
    }

    @Test
    fun `GIVEN a selection WHEN tapping save THEN toggles the saved state of the whole selection`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(VerseSelectionUiEvent.OnToggleSavedClick)
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(1, 2),
                actual = toggledSavedRefs.single().map { it.verseNumber },
            )
            assertTrue(trackedEvents.contains("verse_saved_toggled"))
        }

    private fun verseSelection(verseNumbers: List<Int>): VerseSelection = VerseSelection(
        chapter = testChapter,
        verseNumbers = verseNumbers,
    )

    private fun TestScope.prepareScenario(
        isPro: Boolean = true,
        isColorApplied: Boolean = true,
        isAddVerseNoteBlocked: Boolean = false,
        noteIdByVerse: Map<Int, String> = emptyMap(),
    ) {
        addedCustomColors = mutableListOf()
        toggledSavedRefs = mutableListOf()
        appliedHighlights = mutableListOf()
        clearedCount = mutableListOf()
        trackedEvents = mutableListOf()
        trackedParams = mutableMapOf()
        selection = MutableStateFlow(verseSelection(listOf(1, 2)))
        viewModelStore = ViewModelStore()
        viewModel = ViewModelProvider.create(
            store = viewModelStore,
            factory = viewModelFactory {
                initializer {
                    VerseSelectionViewModel(
                        observeVerseSelection = { selection },
                        clearVerseSelection = { clearedCount += Unit },
                        observeChapterAnnotations = { _ ->
                            flowOf(
                                ChapterAnnotations(
                                    highlightColorByVerse = emptyMap(),
                                    savedVerseNumbers = emptySet(),
                                    noteIdByVerse = noteIdByVerse,
                                    noteVerseNumbersById = noteIdByVerse.entries.groupBy(
                                        keySelector = { it.value },
                                        valueTransform = { it.key },
                                    ),
                                ),
                            )
                        },
                        observeHighlightPalette = { flowOf(emptyList()) },
                        applyHighlightColor = { refs, color ->
                            appliedHighlights += refs to color
                            isColorApplied
                        },
                        addCustomHighlightColor = { color -> addedCustomColors += color },
                        toggleSavedVerses = { refs ->
                            toggledSavedRefs += refs
                            true
                        },
                        observeIsProUser = { flowOf(isPro) },
                        getVersesShareContent = { _, _, _ ->
                            VersesShareContentModel(
                                text = "Verse text",
                                reference = "Gênesis 3:1-2",
                                versionAbbreviation = "ARC",
                            )
                        },
                        shouldBlockAddVerseNote = { isAddVerseNoteBlocked },
                        getMaxFreeVerseNotesAmount = { MAX_FREE_VERSE_NOTES },
                        platform = Platform.Android,
                        navigator = navigator,
                        trackEvent = { name, params ->
                            trackedEvents += name
                            trackedParams[name] = params
                        },
                    )
                }
            },
        )[VerseSelectionViewModel::class]
        actions = mutableListOf<VerseSelectionUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
    }

    private companion object {
        const val MAX_FREE_VERSE_NOTES = 3
    }
}
