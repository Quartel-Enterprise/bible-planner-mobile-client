package com.quare.bibleplanner.feature.verse.share.presentation

import com.quare.bibleplanner.core.books.domain.model.VersesShareContentModel
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.route.ShareVerseImageNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardBackground
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiAction
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiEvent
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
import kotlin.test.assertTrue

internal class ShareVerseViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val verseNumbers = listOf(1, 2)
    private lateinit var viewModel: ShareVerseViewModel
    private lateinit var actions: List<ShareVerseUiAction>
    private val navigator = Navigator()
    private lateinit var commands: List<NavigationCommand>
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
    fun `loads the passage into the card`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = "1 Primeiro 2 Segundo",
            actual = viewModel.uiState.value.quote,
        )
        assertTrue(viewModel.uiState.value.isReady)
    }

    @Test
    fun `sharing as text emits the formatted message`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ShareVerseUiEvent.OnShareAsTextClick)
        runCurrent()

        // Then
        val action = actions.filterIsInstance<ShareVerseUiAction.ShareText>().single()
        assertEquals(
            expected = "1 Primeiro 2 Segundo",
            actual = action.content.text,
        )
        assertEquals(
            expected = "Gênesis 3:1-2",
            actual = action.content.reference,
        )
        assertEquals(
            expected = "text",
            actual = trackedEvents.single { it.first == "verse_shared" }.second["format"],
        )
    }

    @Test
    fun `sharing as an image opens the composer for the same passage`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ShareVerseUiEvent.OnShareAsImageClick)
        runCurrent()

        // Then
        assertEquals(
            expected = NavigationCommand.Navigate(
                ShareVerseImageNavRoute(
                    bookId = BookId.GEN.name,
                    chapterNumber = 3,
                    verseNumbers = verseNumbers,
                ),
            ),
            actual = commands.single(),
        )
    }

    @Test
    fun `sharing the rendered card reports the image format`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ShareVerseUiEvent.OnShareImageReady(byteArrayOf(1, 2, 3)))
        runCurrent()

        // Then
        val action = actions.filterIsInstance<ShareVerseUiAction.ShareImage>().single()
        assertTrue(action.imageBytes.contentEquals(byteArrayOf(1, 2, 3)))
        assertEquals(
            expected = "image",
            actual = trackedEvents.single { it.first == "verse_shared" }.second["format"],
        )
    }

    @Test
    fun `changing the background keeps the chosen font in the style event`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ShareVerseUiEvent.OnBackgroundClick(ShareCardBackground.FOREST))
        runCurrent()

        // Then
        assertEquals(
            expected = ShareCardBackground.FOREST,
            actual = viewModel.uiState.value.background,
        )
        assertEquals(
            expected = "forest",
            actual = trackedEvents.single { it.first == "verse_share_style_changed" }.second["background"],
        )
    }

    private fun TestScope.prepareScenario() {
        trackedEvents = mutableListOf()
        viewModel = ShareVerseViewModel(
            route = ShareVerseNavRoute(
                bookId = BookId.GEN.name,
                chapterNumber = 3,
                verseNumbers = verseNumbers,
            ),
            getVersesShareContent = { _, _, _ ->
                VersesShareContentModel(
                    text = "1 Primeiro 2 Segundo",
                    reference = "Gênesis 3:1-2",
                    versionAbbreviation = "ARC",
                )
            },
            platform = Platform.Android,
            navigator = navigator,
            trackEvent = { name, params -> trackedEvents += name to params },
        )
        actions = mutableListOf<ShareVerseUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
    }
}
