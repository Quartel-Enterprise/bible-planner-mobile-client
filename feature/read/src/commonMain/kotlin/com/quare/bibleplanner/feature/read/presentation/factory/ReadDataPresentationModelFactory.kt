package com.quare.bibleplanner.feature.read.presentation.factory

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.usecase.GetChapterIdUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedBibleFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetVersesWithTextsByChapterIdFlowUseCase
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loadable.Loadable.Loaded
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveChapterAnnotations
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.usecase.GetReadNavigationSuggestionsModelUseCase
import com.quare.bibleplanner.feature.read.presentation.mapper.ChapterVersesUiModelMapper
import com.quare.bibleplanner.feature.read.presentation.model.ChapterLoadResult
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadDataUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.jetbrains.compose.resources.StringResource

@OptIn(ExperimentalCoroutinesApi::class)
internal class ReadDataPresentationModelFactory(
    private val getSelectedVersionIdFlow: GetSelectedVersionIdFlowUseCase,
    private val getChapterId: GetChapterIdUseCase,
    private val getVersesWithTextsByChapterIdFlow: GetVersesWithTextsByChapterIdFlowUseCase,
    private val getSelectedBibleFlow: GetSelectedBibleFlowUseCase,
    private val getReadNavigationSuggestionsModelFlow: GetReadNavigationSuggestionsModelUseCase,
    private val observeChapterAnnotations: ObserveChapterAnnotations,
    private val chapterVersesUiModelMapper: ChapterVersesUiModelMapper,
) : ObserveReadData {
    override fun invoke(
        bookId: BookId,
        chapterNumber: Int,
        bookStringResource: StringResource,
        isInitiallyRead: Boolean,
        isFromBookDetails: Boolean,
        prependedChapters: List<ReadNavigationSuggestionModel>,
        appendedChapters: List<ReadNavigationSuggestionModel>,
    ): Flow<ReadDataUiModel> = flow {
        getReadNavigationSuggestionsModelFlow(
            shouldForceCanonOrder = isFromBookDetails,
            currentBookId = bookId,
            currentChapterNumber = chapterNumber,
        ).collect { navigationSuggestions ->
            val chapterFlows = prependedChapters.map(::observeChapter) +
                observeChapter(
                    bookId = bookId,
                    chapterNumber = chapterNumber,
                ) +
                appendedChapters.map(::observeChapter)
            emitAll(
                combine(
                    combine(chapterFlows) { results -> results.toList() },
                    getSelectedVersionIdFlow(),
                    observeSelectedBible(),
                ) { chapterResults, versionId, selectedBible ->
                    val chapterResult = chapterResults[prependedChapters.size]
                    val chapter = (chapterResult as? ChapterLoadResult.Loaded)?.chapter
                    ReadDataUiModel(
                        header = ReadHeaderUiModel(
                            bookId = bookId,
                            bookStringResource = bookStringResource,
                            chapterNumber = chapterNumber,
                            isChapterRead = chapter?.isRead ?: isInitiallyRead,
                            navigationSuggestions = navigationSuggestions,
                            versionAbbreviation = Loaded(versionId.uppercase()),
                        ),
                        content = toContent(
                            chapterResult = chapterResult,
                            precedingChapters = chapterResults
                                .take(prependedChapters.size)
                                .mapNotNull { (it as? ChapterLoadResult.Loaded)?.chapter },
                            followingChapters = chapterResults
                                .drop(prependedChapters.size + 1)
                                .mapNotNull { (it as? ChapterLoadResult.Loaded)?.chapter },
                            selectedBible = selectedBible,
                        ),
                    )
                },
            )
        }
    }

    private fun observeSelectedBible(): Flow<Loadable<BibleModel?>> = getSelectedBibleFlow()
        .map<BibleModel?, Loadable<BibleModel?>>(::Loaded)
        .onStart { emit(Loadable.Loading) }

    private fun toContent(
        chapterResult: ChapterLoadResult,
        precedingChapters: List<ReadChapterUiModel>,
        followingChapters: List<ReadChapterUiModel>,
        selectedBible: Loadable<BibleModel?>,
    ): ReadContentUiState = when (chapterResult) {
        ChapterLoadResult.ChapterMissing -> ReadContentUiState.Error.Unknown(
            errorUiEvent = ReadUiEvent.OnRetryClick,
        )

        ChapterLoadResult.TextMissing -> if (selectedBible is Loaded) {
            val bible = selectedBible.value
            ReadContentUiState.Error.ChapterNotFound(
                errorUiEvent = ReadUiEvent.ManageBibleVersions,
                selectedBibleVersionName = bible?.version?.name.orEmpty(),
                downloadStatus = bible?.downloadStatus ?: DownloadStatusModel.NotStarted,
                versionSizeInBytes = bible?.version?.size,
            )
        } else {
            ReadContentUiState.Loading
        }

        is ChapterLoadResult.Loaded -> ReadContentUiState.Success(
            chapters = precedingChapters + chapterResult.chapter + followingChapters,
        )
    }

    private fun observeChapter(chapter: ReadNavigationSuggestionModel): Flow<ChapterLoadResult> = observeChapter(
        bookId = chapter.bookId,
        chapterNumber = chapter.chapterNumber,
    )

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
            // Why: the version leads because annotations are scoped to it; switching version must
            // resubscribe them instead of showing the previous version's annotations.
            getSelectedVersionIdFlow().flatMapLatest { versionId ->
                combine(
                    // Why: Room re-runs the query on any verse-table write, so a version download re-emits
                    // identical rows thousands of times; dropping them avoids rebuilding every verse on main.
                    getVersesWithTextsByChapterIdFlow(chapterId).distinctUntilChanged(),
                    observeChapterAnnotations(
                        ChapterRef(
                            bibleVersionId = versionId,
                            bookId = bookId,
                            chapterNumber = chapterNumber,
                        ),
                    ),
                ) { versesWithTexts, annotations ->
                    val verses = chapterVersesUiModelMapper.map(
                        versesWithTexts = versesWithTexts,
                        versionId = versionId,
                        annotations = annotations,
                    )
                    if (verses.isEmpty()) return@combine ChapterLoadResult.TextMissing
                    ChapterLoadResult.Loaded(
                        ReadChapterUiModel(
                            chapter = ChapterRef(
                                bibleVersionId = versionId,
                                bookId = bookId,
                                chapterNumber = chapterNumber,
                            ),
                            bookStringResource = bookId.toBookNameResource(),
                            isRead = versesWithTexts.all { it.verse.isRead },
                            verses = verses,
                        ),
                    )
                }
            },
        )
    }
}
