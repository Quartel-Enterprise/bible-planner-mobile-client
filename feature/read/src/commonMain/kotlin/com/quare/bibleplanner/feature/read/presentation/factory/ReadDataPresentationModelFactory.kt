package com.quare.bibleplanner.feature.read.presentation.factory

import com.quare.bibleplanner.core.books.domain.usecase.GetChapterIdUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedBibleNameFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetVersesWithTextsByChapterIdFlowUseCase
import com.quare.bibleplanner.core.model.book.BookId
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
) {
    fun create(
        bookId: BookId,
        chapterNumber: Int,
        bookStringResource: StringResource,
        isInitiallyRead: Boolean,
    ): Flow<ReadUiState> = flow {
        getChapterId(
            bookId = bookId,
            chapterNumber = chapterNumber,
        )?.let { chapterId ->
            getReadUiStateFlow(chapterId, bookStringResource, chapterNumber)
        }?.let { readUiStateFlow ->
            emitAll(readUiStateFlow)
        } ?: emit(
            ReadUiState.Error.Unknown(
                bookStringResource = bookStringResource,
                chapterNumber = chapterNumber,
                isChapterRead = isInitiallyRead,
                errorUiEvent = ReadUiEvent.OnRetryClick,
            ),
        )
    }

    private fun getReadUiStateFlow(
        chapterId: Long,
        bookStringResource: StringResource,
        chapterNumber: Int,
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
        )
        if (versesWithTexts.isEmpty()) {
            chapterNotFoundState
        } else {
            val verseUiModels = versesWithTexts.map { verseWithTexts ->
                verseWithTexts.texts.find { it.bibleVersionId == versionId }?.text?.let { safeText ->
                    VerseUiModel(
                        number = verseWithTexts.verse.number,
                        text = safeText,
                    )
                } ?: return@combine chapterNotFoundState
            }
            ReadUiState.Success(
                bookStringResource = bookStringResource,
                chapterNumber = chapterNumber,
                verses = verseUiModels,
                isChapterRead = isChapterRead,
            )
        }
    }
}
