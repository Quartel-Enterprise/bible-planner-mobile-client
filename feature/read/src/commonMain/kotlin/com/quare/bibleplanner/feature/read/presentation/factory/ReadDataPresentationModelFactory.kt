package com.quare.bibleplanner.feature.read.presentation.factory

import com.quare.bibleplanner.core.books.domain.usecase.GetChapterIdUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetVersesWithTextsByChapterIdFlowUseCase
import com.quare.bibleplanner.core.model.book.BookId
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
) {
    fun create(
        bookId: BookId,
        chapterNumber: Int,
        bookStringResource: StringResource,
        isInitiallyRead: Boolean,
    ): Flow<ReadUiState> = flow {
        val errorState = ReadUiState.Error(
            bookStringResource = bookStringResource,
            chapterNumber = chapterNumber,
            isChapterRead = isInitiallyRead,
        )
        getChapterId(
            bookId = bookId,
            chapterNumber = chapterNumber,
        )?.let { chapterId ->
            getReadUiStateFlow(chapterId, errorState, bookStringResource, chapterNumber)
        }?.let { readUiStateFlow ->
            emitAll(readUiStateFlow)
        } ?: emit(errorState)
    }

    private fun getReadUiStateFlow(
        chapterId: Long,
        errorState: ReadUiState.Error,
        bookStringResource: StringResource,
        chapterNumber: Int,
    ): Flow<ReadUiState> = combine(
        getSelectedVersionIdFlow(),
        getVersesWithTextsByChapterIdFlow(chapterId),
    ) { versionId, versesWithTexts ->
        if (versesWithTexts.isEmpty()) {
            errorState
        } else {
            val verseUiModels = versesWithTexts.map { verseWithTexts ->
                verseWithTexts.texts.find { it.bibleVersionId == versionId }?.text?.let { safeText ->
                    VerseUiModel(
                        number = verseWithTexts.verse.number,
                        text = safeText,
                    )
                } ?: return@combine errorState
            }
            ReadUiState.Success(
                bookStringResource = bookStringResource,
                chapterNumber = chapterNumber,
                verses = verseUiModels,
                isChapterRead = versesWithTexts.all { it.verse.isRead },
            )
        }
    }
}
