package com.quare.bibleplanner.feature.read.presentation.mapper

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderRulerLines
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadDataUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import org.jetbrains.compose.resources.StringResource
import software.quare.loadable.Loadable

internal class ReadUiStateMapper {
    fun map(
        data: ReadDataUiModel,
        settings: ReaderSettingsModel,
        selection: VerseSelection?,
        overrides: Map<ChapterLocationModel, Boolean>,
        isLoadingPreviousChapter: Boolean,
        isLoadingNextChapter: Boolean,
    ): ReadUiState = ReadUiState(
        header = data.header.withReadOverride(overrides),
        content = data.content.withSelection(selection).withReadOverrides(overrides),
        settings = settings,
        isLoadingPreviousChapter = isLoadingPreviousChapter,
        isLoadingNextChapter = isLoadingNextChapter,
        dayCompletionBanner = null,
        verseFocus = null,
        isOpeningChapterStudy = false,
        isChapterStudyBeside = false,
    )

    fun dropReconciledOverrides(
        overrides: Map<ChapterLocationModel, Boolean>,
        data: ReadDataUiModel,
    ): Map<ChapterLocationModel, Boolean> = if (overrides.isEmpty()) {
        overrides
    } else {
        overrides.filterNot { (key, override) -> data.isAuthoritativelyRead(key) == override }
    }

    fun createLoadingState(
        bookId: BookId,
        bookStringResource: StringResource,
        chapterNumber: Int,
        isChapterRead: Boolean,
    ): ReadUiState = ReadUiState(
        header = ReadHeaderUiModel(
            bookId = bookId,
            bookStringResource = bookStringResource,
            chapterNumber = chapterNumber,
            isChapterRead = isChapterRead,
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
    )

    private fun ReadHeaderUiModel.withReadOverride(overrides: Map<ChapterLocationModel, Boolean>): ReadHeaderUiModel {
        val override = overrides[ChapterLocationModel(bookId = bookId, chapterNumber = chapterNumber)] ?: return this
        return copy(isChapterRead = override)
    }

    private fun ReadContentUiState.withReadOverrides(
        overrides: Map<ChapterLocationModel, Boolean>,
    ): ReadContentUiState = if (this !is ReadContentUiState.Success || overrides.isEmpty()) {
        this
    } else {
        copy(chapters = chapters.map { chapter -> chapter.withReadOverride(overrides) })
    }

    private fun ReadChapterUiModel.withReadOverride(overrides: Map<ChapterLocationModel, Boolean>): ReadChapterUiModel {
        val key = ChapterLocationModel(bookId = chapter.bookId, chapterNumber = chapter.chapterNumber)
        val override = overrides[key] ?: return this
        return copy(isRead = override)
    }

    private fun ReadDataUiModel.isAuthoritativelyRead(key: ChapterLocationModel): Boolean? {
        if (header.bookId == key.bookId && header.chapterNumber == key.chapterNumber) return header.isChapterRead
        return (content as? ReadContentUiState.Success)
            ?.chapters
            ?.firstOrNull { it.chapter.bookId == key.bookId && it.chapter.chapterNumber == key.chapterNumber }
            ?.isRead
    }

    private fun ReadContentUiState.withSelection(selection: VerseSelection?): ReadContentUiState =
        if (this !is ReadContentUiState.Success) {
            this
        } else {
            copy(chapters = chapters.map { chapter -> chapter.withSelection(selection) })
        }

    private fun ReadChapterUiModel.withSelection(selection: VerseSelection?): ReadChapterUiModel {
        val selectedVerseNumbers = selection
            ?.takeIf { it.chapter == chapter }
            ?.verseNumbers
            .orEmpty()
        return copy(
            verses = verses.map { verse ->
                verse.copy(isSelected = verse.number in selectedVerseNumbers)
            },
        )
    }
}
