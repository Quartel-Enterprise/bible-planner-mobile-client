package com.quare.bibleplanner.feature.read.presentation.mapper

import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderRulerLines
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.fixture.readChapter
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadDataUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import software.quare.loadable.Loadable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ReadUiStateMapperTest {
    private val mapper = ReadUiStateMapper()
    private val header = ReadHeaderUiModel(
        bookId = BookId.GEN,
        bookStringResource = BookId.GEN.toBookNameResource(),
        chapterNumber = 1,
        isChapterRead = false,
        navigationSuggestions = ReadNavigationSuggestionsModel(
            previous = null,
            next = null,
        ),
        versionAbbreviation = Loadable.Loaded("WEB"),
    )
    private val firstChapter = readChapter(
        chapterNumber = 1,
        verseCount = 3,
    )
    private val secondChapter = readChapter(
        chapterNumber = 2,
        verseCount = 3,
    )
    private val data = ReadDataUiModel(
        header = header,
        content = ReadContentUiState.Success(chapters = listOf(firstChapter, secondChapter)),
    )
    private val settings = ReaderSettingsModel(
        fontSizeSp = ReaderFontSize.DEFAULT,
        font = ReaderFont.LORA,
        isRulerEnabled = true,
        rulerLines = ReaderRulerLines.DEFAULT,
        isFocusedVerseEnabled = false,
        isVerticalReadingEnabled = true,
        isNoteIconEnabled = false,
    )

    @Test
    fun `GIVEN no selection nor overrides WHEN mapping THEN carries the data and settings into the state`() {
        // Given
        val overrides = emptyMap<ChapterLocationModel, Boolean>()

        // When
        val state = mapper.map(
            data = data,
            settings = settings,
            selection = null,
            overrides = overrides,
            isLoadingPreviousChapter = true,
            isLoadingNextChapter = false,
        )

        // Then
        assertEquals(
            expected = ReadUiState(
                header = header,
                content = data.content,
                settings = settings,
                isLoadingPreviousChapter = true,
                isLoadingNextChapter = false,
                dayCompletionBanner = null,
                verseFocus = null,
                isOpeningChapterStudy = false,
                isChapterStudyBeside = false,
            ),
            actual = state,
        )
    }

    @Test
    fun `GIVEN an override for the open chapter WHEN mapping THEN applies it to the header and that chapter`() {
        // Given
        val overrides = mapOf(location(1) to true)

        // When
        val state = mapper.map(
            data = data,
            settings = settings,
            selection = null,
            overrides = overrides,
            isLoadingPreviousChapter = false,
            isLoadingNextChapter = false,
        )

        // Then
        assertTrue(state.header.isChapterRead)
        assertEquals(
            expected = listOf(true, false),
            actual = state.successChapters().map(ReadChapterUiModel::isRead),
        )
    }

    @Test
    fun `GIVEN an override for an appended chapter WHEN mapping THEN applies it to that chapter only`() {
        // Given
        val overrides = mapOf(location(2) to true)

        // When
        val state = mapper.map(
            data = data,
            settings = settings,
            selection = null,
            overrides = overrides,
            isLoadingPreviousChapter = false,
            isLoadingNextChapter = false,
        )

        // Then
        assertFalse(state.header.isChapterRead)
        assertEquals(
            expected = listOf(false, true),
            actual = state.successChapters().map(ReadChapterUiModel::isRead),
        )
    }

    @Test
    fun `GIVEN a loading chapter and an override WHEN mapping THEN applies it to the header and keeps loading`() {
        // Given
        val loadingData = data.copy(content = ReadContentUiState.Loading)
        val overrides = mapOf(location(1) to true)

        // When
        val state = mapper.map(
            data = loadingData,
            settings = settings,
            selection = null,
            overrides = overrides,
            isLoadingPreviousChapter = false,
            isLoadingNextChapter = false,
        )

        // Then
        assertTrue(state.header.isChapterRead)
        assertEquals(
            expected = ReadContentUiState.Loading,
            actual = state.content,
        )
    }

    @Test
    fun `GIVEN a selection in one chapter WHEN mapping THEN marks its verses selected in that chapter only`() {
        // Given
        val selection = VerseSelection(
            chapter = secondChapter.chapter,
            verseNumbers = listOf(1, 3),
        )

        // When
        val state = mapper.map(
            data = data,
            settings = settings,
            selection = selection,
            overrides = emptyMap(),
            isLoadingPreviousChapter = false,
            isLoadingNextChapter = false,
        )

        // Then
        assertEquals(
            expected = listOf(
                listOf(false, false, false),
                listOf(true, false, true),
            ),
            actual = state.successChapters().map(::selectedFlags),
        )
    }

    @Test
    fun `GIVEN a selection in another version of the chapter WHEN mapping THEN selects no verse`() {
        // Given
        val selection = VerseSelection(
            chapter = ChapterRef(
                bibleVersionId = "ACF",
                bookId = BookId.GEN,
                chapterNumber = 1,
            ),
            verseNumbers = listOf(1),
        )

        // When
        val state = mapper.map(
            data = data,
            settings = settings,
            selection = selection,
            overrides = emptyMap(),
            isLoadingPreviousChapter = false,
            isLoadingNextChapter = false,
        )

        // Then
        assertTrue(state.successChapters().flatMap(::selectedFlags).none { it })
    }

    @Test
    fun `GIVEN verses selected in the data WHEN mapping without a selection THEN clears them`() {
        // Given
        val selectedChapter = firstChapter.copy(
            verses = firstChapter.verses.map { verse -> verse.copy(isSelected = true) },
        )
        val selectedData = data.copy(content = ReadContentUiState.Success(chapters = listOf(selectedChapter)))

        // When
        val state = mapper.map(
            data = selectedData,
            settings = settings,
            selection = null,
            overrides = emptyMap(),
            isLoadingPreviousChapter = false,
            isLoadingNextChapter = false,
        )

        // Then
        assertEquals(
            expected = listOf(listOf(false, false, false)),
            actual = state.successChapters().map(::selectedFlags),
        )
    }

    @Test
    fun `GIVEN no overrides WHEN dropping the reconciled ones THEN returns none`() {
        // Given
        val overrides = emptyMap<ChapterLocationModel, Boolean>()

        // When
        val remaining = mapper.dropReconciledOverrides(
            overrides = overrides,
            data = data,
        )

        // Then
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun `GIVEN an override the header agrees with WHEN dropping the reconciled ones THEN drops it`() {
        // Given
        val agreeingData = data.copy(header = header.copy(isChapterRead = true))
        val overrides = mapOf(location(1) to true)

        // When
        val remaining = mapper.dropReconciledOverrides(
            overrides = overrides,
            data = agreeingData,
        )

        // Then
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun `GIVEN an override the header disagrees with WHEN dropping the reconciled ones THEN keeps it`() {
        // Given
        val overrides = mapOf(location(1) to true)

        // When
        val remaining = mapper.dropReconciledOverrides(
            overrides = overrides,
            data = data,
        )

        // Then
        assertEquals(
            expected = overrides,
            actual = remaining,
        )
    }

    @Test
    fun `GIVEN overrides on appended chapters WHEN dropping the reconciled ones THEN keeps the pending one`() {
        // Given
        val readSecondChapter = secondChapter.copy(isRead = true)
        val thirdChapter = readChapter(
            chapterNumber = 3,
            verseCount = 1,
        )
        val verticalData = data.copy(
            content = ReadContentUiState.Success(chapters = listOf(firstChapter, readSecondChapter, thirdChapter)),
        )
        val overrides = mapOf(
            location(2) to true,
            location(3) to true,
        )

        // When
        val remaining = mapper.dropReconciledOverrides(
            overrides = overrides,
            data = verticalData,
        )

        // Then
        assertEquals(
            expected = mapOf(location(3) to true),
            actual = remaining,
        )
    }

    @Test
    fun `GIVEN an override for a chapter not shown WHEN dropping the reconciled ones THEN keeps it`() {
        // Given
        val loadingData = data.copy(content = ReadContentUiState.Loading)
        val overrides = mapOf(location(2) to false)

        // When
        val remaining = mapper.dropReconciledOverrides(
            overrides = overrides,
            data = loadingData,
        )

        // Then
        assertEquals(
            expected = overrides,
            actual = remaining,
        )
    }

    @Test
    fun `GIVEN a chapter to open WHEN creating the loading state THEN shows its header with default settings`() {
        // Given
        val bookStringResource = BookId.EXO.toBookNameResource()

        // When
        val state = mapper.createLoadingState(
            bookId = BookId.EXO,
            bookStringResource = bookStringResource,
            chapterNumber = 4,
            isChapterRead = true,
        )

        // Then
        assertEquals(
            expected = ReadUiState(
                header = ReadHeaderUiModel(
                    bookId = BookId.EXO,
                    bookStringResource = bookStringResource,
                    chapterNumber = 4,
                    isChapterRead = true,
                    navigationSuggestions = ReadNavigationSuggestionsModel(
                        previous = null,
                        next = null,
                    ),
                    versionAbbreviation = Loadable.Loading,
                ),
                content = ReadContentUiState.Loading,
                settings = ReaderSettingsModel(
                    fontSizeSp = ReaderFontSize.DEFAULT,
                    font = ReaderFont.LORA,
                    isRulerEnabled = false,
                    rulerLines = ReaderRulerLines.DEFAULT,
                    isFocusedVerseEnabled = false,
                    isVerticalReadingEnabled = false,
                    isNoteIconEnabled = true,
                ),
                isLoadingPreviousChapter = false,
                isLoadingNextChapter = false,
                dayCompletionBanner = null,
                verseFocus = null,
                isOpeningChapterStudy = false,
                isChapterStudyBeside = false,
            ),
            actual = state,
        )
    }

    private fun location(chapterNumber: Int): ChapterLocationModel = ChapterLocationModel(
        bookId = BookId.GEN,
        chapterNumber = chapterNumber,
    )

    private fun selectedFlags(chapter: ReadChapterUiModel): List<Boolean> = chapter.verses.map { it.isSelected }

    private fun ReadUiState.successChapters(): List<ReadChapterUiModel> =
        (content as ReadContentUiState.Success).chapters
}
