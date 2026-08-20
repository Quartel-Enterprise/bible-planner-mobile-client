package com.quare.bibleplanner.feature.read.presentation.factory

import com.quare.bibleplanner.core.books.domain.usecase.GetChapterIdUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedBibleFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetVersesWithTextsByChapterIdFlowUseCase
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.verseannotations.domain.model.ChapterAnnotations
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveChapterAnnotations
import com.quare.bibleplanner.feature.read.domain.usecase.GetReadNavigationSuggestionsModelUseCase
import com.quare.bibleplanner.feature.read.presentation.model.ChapterLoadResult
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadDataUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.VerseUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.StringResource

/**
 * Assembles what the reader renders: the chapter's verses decorated with the user's annotations,
 * plus the header the screen keeps showing even when the text itself failed to load.
 */
class ReadDataPresentationModelFactory(
    private val getSelectedVersionIdFlow: GetSelectedVersionIdFlowUseCase,
    private val getChapterId: GetChapterIdUseCase,
    private val getVersesWithTextsByChapterIdFlow: GetVersesWithTextsByChapterIdFlowUseCase,
    private val getSelectedBibleFlow: GetSelectedBibleFlowUseCase,
    private val getReadNavigationSuggestionsModelFlow: GetReadNavigationSuggestionsModelUseCase,
    private val observeChapterAnnotations: ObserveChapterAnnotations,
) : ObserveReadData {
    /**
     * With [isVerticalReadingEnabled] on, the next chapter is appended to the same flow so the reader
     * keeps scrolling into it instead of stopping at the end of this one.
     */
    override fun invoke(
        bookId: BookId,
        chapterNumber: Int,
        bookStringResource: StringResource,
        isInitiallyRead: Boolean,
        isFromBookDetails: Boolean,
        isVerticalReadingEnabled: Boolean,
    ): Flow<ReadDataUiModel> = flow {
        getReadNavigationSuggestionsModelFlow(
            shouldForceCanonOrder = isFromBookDetails,
            currentBookId = bookId,
            currentChapterNumber = chapterNumber,
        ).collect { navigationSuggestions ->
            val nextChapter = navigationSuggestions.next.takeIf { isVerticalReadingEnabled }
            emitAll(
                combine(
                    observeChapter(
                        bookId = bookId,
                        chapterNumber = chapterNumber,
                    ),
                    nextChapter
                        ?.let {
                            observeChapter(
                                bookId = it.bookId,
                                chapterNumber = it.chapterNumber,
                            )
                        } ?: flowOf(null),
                    getSelectedBibleFlow(),
                ) { chapterResult, appendedChapterResult, selectedBible ->
                    val chapter = (chapterResult as? ChapterLoadResult.Loaded)?.chapter
                    ReadDataUiModel(
                        header = ReadHeaderUiModel(
                            bookId = bookId,
                            bookStringResource = bookStringResource,
                            chapterNumber = chapterNumber,
                            isChapterRead = chapter?.isRead ?: isInitiallyRead,
                            navigationSuggestions = navigationSuggestions,
                            versionAbbreviation = selectedBible
                                ?.version
                                ?.id
                                ?.uppercase()
                                .orEmpty(),
                        ),
                        content = toContent(
                            chapterResult = chapterResult,
                            appendedChapter = (appendedChapterResult as? ChapterLoadResult.Loaded)?.chapter,
                            selectedBibleVersionName = selectedBible?.version?.name.orEmpty(),
                            downloadStatus = selectedBible?.downloadStatus ?: DownloadStatusModel.NotStarted,
                            versionSizeInBytes = selectedBible?.version?.size,
                        ),
                    )
                },
            )
        }
    }

    private fun toContent(
        chapterResult: ChapterLoadResult,
        appendedChapter: ReadChapterUiModel?,
        selectedBibleVersionName: String,
        downloadStatus: DownloadStatusModel,
        versionSizeInBytes: Long?,
    ): ReadContentUiState = when (chapterResult) {
        ChapterLoadResult.ChapterMissing -> ReadContentUiState.Error.Unknown(
            errorUiEvent = ReadUiEvent.OnRetryClick,
        )

        ChapterLoadResult.TextMissing -> ReadContentUiState.Error.ChapterNotFound(
            errorUiEvent = ReadUiEvent.ManageBibleVersions,
            selectedBibleVersionName = selectedBibleVersionName,
            downloadStatus = downloadStatus,
            versionSizeInBytes = versionSizeInBytes,
        )

        is ChapterLoadResult.Loaded -> ReadContentUiState.Success(
            chapters = listOfNotNull(chapterResult.chapter, appendedChapter),
        )
    }

    private fun observeChapter(
        bookId: BookId,
        chapterNumber: Int,
    ): Flow<ChapterLoadResult> = flow {
        val chapterId = getChapterId(
            bookId = bookId,
            chapterNumber = chapterNumber,
        )
        if (chapterId == null) {
            emit(ChapterLoadResult.ChapterMissing)
            return@flow
        }
        emitAll(
            combine(
                getSelectedVersionIdFlow(),
                getVersesWithTextsByChapterIdFlow(chapterId),
                observeChapterAnnotations(
                    bookId = bookId,
                    chapterNumber = chapterNumber,
                ),
            ) { versionId, versesWithTexts, annotations ->
                if (versesWithTexts.isEmpty()) return@combine ChapterLoadResult.TextMissing
                val verses = versesWithTexts.map { verseWithTexts ->
                    val verseText = verseWithTexts.texts.find { it.bibleVersionId == versionId }
                        ?: return@combine ChapterLoadResult.TextMissing
                    verseWithTexts.verse.number.toVerseUiModel(
                        heading = verseText.heading,
                        text = verseText.text,
                        annotations = annotations,
                    )
                }
                ChapterLoadResult.Loaded(
                    ReadChapterUiModel(
                        bookId = bookId,
                        bookStringResource = bookId.toBookNameResource(),
                        chapterNumber = chapterNumber,
                        isRead = versesWithTexts.all { it.verse.isRead },
                        verses = verses,
                    ),
                )
            },
        )
    }

    private fun Int.toVerseUiModel(
        heading: String?,
        text: String,
        annotations: ChapterAnnotations,
    ): VerseUiModel = VerseUiModel(
        number = this,
        heading = heading,
        text = text,
        isSelected = false,
        highlightColor = annotations.highlightColorByVerse[this],
        isSaved = this in annotations.savedVerseNumbers,
        noteId = annotations.noteIdByVerse[this],
    )
}
