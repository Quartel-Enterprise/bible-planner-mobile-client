package com.quare.bibleplanner.feature.read.presentation.factory

import com.quare.bibleplanner.core.books.domain.usecase.GetChapterIdUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedBibleNameFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetVersesWithTextsByChapterIdFlowUseCase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.domain.usecase.GetReadNavigationSuggestionsModelUseCase
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.feature.read.presentation.model.VerseUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.jetbrains.compose.resources.StringResource

class ReadDataPresentationModelFactory(
    private val getSelectedVersionIdFlow: GetSelectedVersionIdFlowUseCase,
    private val getChapterId: GetChapterIdUseCase,
    private val getVersesWithTextsByChapterIdFlow: GetVersesWithTextsByChapterIdFlowUseCase,
    private val getSelectedBibleNameFlow: GetSelectedBibleNameFlowUseCase,
    private val getReadNavigationSuggestionsModelFlow: GetReadNavigationSuggestionsModelUseCase,
) {
    fun create(
        bookId: BookId,
        chapterNumber: Int,
        bookStringResource: StringResource,
        isInitiallyRead: Boolean,
        isFromBookDetails: Boolean,
    ): Flow<ReadUiState> = flow {
        getReadNavigationSuggestionsModelFlow(
            shouldForceChronologicalOrder = isFromBookDetails,
            currentBookId = bookId,
            currentChapterNumber = chapterNumber,
        ).collect { navigationSuggestions ->
            getChapterId(
                bookId = bookId,
                chapterNumber = chapterNumber,
            )?.let { chapterId ->
                getReadUiStateFlow(
                    chapterId = chapterId,
                    bookStringResource = bookStringResource,
                    chapterNumber = chapterNumber,
                    navigationSuggestions = navigationSuggestions,
                )
            }?.let { readUiStateFlow ->
                emitAll(readUiStateFlow)
            } ?: emit(
                ReadUiState.Error.Unknown(
                    bookStringResource = bookStringResource,
                    chapterNumber = chapterNumber,
                    isChapterRead = isInitiallyRead,
                    errorUiEvent = ReadUiEvent.OnRetryClick,
                    navigationSuggestions = navigationSuggestions,
                ),
            )
        }
    }

    private fun getReadUiStateFlow(
        chapterId: Long,
        bookStringResource: StringResource,
        chapterNumber: Int,
        navigationSuggestions: ReadNavigationSuggestionsModel,
    ): Flow<ReadUiState> = combine(
        getSelectedVersionIdFlow(),
        getVersesWithTextsByChapterIdFlow(chapterId),
        getSelectedBibleNameFlow(),
    ) { versionId, versesWithTexts, selectedBibleName ->
        val isChapterRead = versesWithTexts.all { it.verse.isRead }
        val chapterNotFoundState = ReadUiState.Error.ChapterNotFound(
            bookStringResource = bookStringResource,
            chapterNumber = chapterNumber,
            isChapterRead = isChapterRead,
            selectedBibleVersionName = selectedBibleName,
            errorUiEvent = ReadUiEvent.ManageBibleVersions,
            navigationSuggestions = navigationSuggestions,
        )
        if (versesWithTexts.isEmpty()) {
            chapterNotFoundState
        } else {
            val verseUiModels = versesWithTexts.map { verseWithTexts ->
                verseWithTexts.texts.find { it.bibleVersionId == versionId }?.text?.let { safeText ->
                    VerseUiModel(
                        number = verseWithTexts.verse.number,
                        text = safeText,
                        isSelected = false, // TODO: will be used in the future for the copy/share feature
                    )
                } ?: return@combine chapterNotFoundState
            }
            ReadUiState.Success(
                bookStringResource = bookStringResource,
                chapterNumber = chapterNumber,
                verses = verseUiModels,
                isChapterRead = isChapterRead,
                navigationSuggestions = navigationSuggestions,
            )
        }
    }
}
