package com.quare.bibleplanner.feature.bookdetails.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.books.domain.usecase.GetBookByIdFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdateBookReadStatusUseCase
import com.quare.bibleplanner.core.books.presentation.mapper.BookGroupMapper
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.loginnudge.domain.usecase.RequestLoginNudgeIfNeeded
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.route.BookDetailsNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.review.domain.model.ReviewTrigger
import com.quare.bibleplanner.core.review.domain.usecase.RequestReviewIfNeeded
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiAction
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiEvent
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiState
import com.quare.bibleplanner.feature.bookdetails.presentation.utils.toSynopsisResource
import com.quare.bibleplanner.ui.utils.observe
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class BookDetailsViewModel(
    route: BookDetailsNavRoute,
    private val booksRepository: BooksRepository,
    private val bookGroupMapper: BookGroupMapper,
    private val markBookRead: UpdateBookReadStatusUseCase,
    private val requestLoginNudgeIfNeeded: RequestLoginNudgeIfNeeded,
    private val requestReviewIfNeeded: RequestReviewIfNeeded,
    trackEvent: TrackEvent,
    getBookByIdFlow: GetBookByIdFlowUseCase,
    val platform: Platform,
) : TrackedViewModel<BookDetailsUiEvent>(trackEvent) {
    private val bookId = BookId.valueOf(route.bookId)

    private val _uiState = MutableStateFlow<BookDetailsUiState>(BookDetailsUiState.Loading)
    val uiState: StateFlow<BookDetailsUiState> = _uiState

    private val _uiAction = MutableSharedFlow<BookDetailsUiAction>()
    val uiAction: SharedFlow<BookDetailsUiAction> = _uiAction

    private val successState get() = uiState.value as? BookDetailsUiState.Success

    private var previousAllChaptersRead: Boolean? = null

    init {
        observe(getBookByIdFlow(bookId)) { fetchedBook ->
            val book = fetchedBook ?: return@observe
            val totalChapters = book.chapters.size
            val readChapters = book.chapters.count { it.isRead }
            val progress = if (totalChapters > 0) readChapters.toFloat() / totalChapters else 0f
            val areAllChaptersRead = totalChapters > 0 && readChapters == totalChapters
            val justCompletedBook = previousAllChaptersRead == false && areAllChaptersRead
            previousAllChaptersRead = areAllChaptersRead

            val bookGroup = bookGroupMapper.fromBookId(book.id)
            val bookCategoryName = getString(bookGroup.titleRes)

            _uiState.update { currentState ->
                val isSynopsisExpanded = (currentState as? BookDetailsUiState.Success)?.isSynopsisExpanded ?: false
                BookDetailsUiState.Success(
                    id = book.id,
                    nameStringResource = book.id.toBookNameResource(),
                    synopsisStringResource = book.id.toSynopsisResource(),
                    chapters = book.chapters,
                    progress = progress,
                    readChaptersCount = readChapters,
                    totalChaptersCount = totalChapters,
                    areAllChaptersRead = areAllChaptersRead,
                    isFavorite = book.isFavorite,
                    bookGroup = bookGroup,
                    bookCategoryName = bookCategoryName,
                    isSynopsisExpanded = isSynopsisExpanded,
                )
            }

            if (justCompletedBook) {
                requestReviewIfNeeded(ReviewTrigger.BOOK_COMPLETED)
            }
        }
    }

    override fun handleEvent(event: BookDetailsUiEvent) {
        when (event) {
            BookDetailsUiEvent.OnBackClick -> {
                viewModelScope.launch {
                    _uiAction.emit(BookDetailsUiAction.NavigateBack)
                }
            }

            BookDetailsUiEvent.OnToggleFavorite -> {
                successState?.let {
                    val isFavorite = !it.isFavorite
                    trackEvent(
                        name = AnalyticsEventNames.BOOK_FAVORITE_TOGGLED,
                        params = mapOf(
                            AnalyticsParams.BOOK_ID to bookId.name.lowercase(),
                            AnalyticsParams.IS_FAVORITE to isFavorite,
                            AnalyticsParams.SOURCE to SOURCE_BOOK_DETAILS,
                        ),
                    )
                    viewModelScope.launch {
                        booksRepository.updateBookFavoriteStatus(
                            bookId = bookId,
                            isFavorite = isFavorite,
                        )
                        requestLoginNudgeIfNeeded()
                    }
                }
            }

            BookDetailsUiEvent.OnToggleSynopsisExpanded -> {
                successState?.let {
                    val isExpanded = !it.isSynopsisExpanded
                    _uiState.update { currentState ->
                        (currentState as? BookDetailsUiState.Success)?.copy(
                            isSynopsisExpanded = isExpanded,
                        ) ?: currentState
                    }
                    trackEvent(
                        name = AnalyticsEventNames.SYNOPSIS_TOGGLED,
                        params = mapOf(
                            AnalyticsParams.BOOK_ID to bookId.name.lowercase(),
                            AnalyticsParams.IS_EXPANDED to isExpanded,
                        ),
                    )
                }
            }

            BookDetailsUiEvent.OnToggleAllChapters -> {
                successState?.let {
                    val isRead = !it.areAllChaptersRead
                    trackEvent(
                        name = AnalyticsEventNames.BOOK_READ_TOGGLED,
                        params = mapOf(
                            AnalyticsParams.BOOK_ID to bookId.name.lowercase(),
                            AnalyticsParams.IS_READ to isRead,
                        ),
                    )
                    viewModelScope.launch {
                        markBookRead(
                            bookId = bookId,
                            isRead = isRead,
                        )
                        requestLoginNudgeIfNeeded()
                    }
                }
            }

            is BookDetailsUiEvent.OnChapterClick -> {
                successState?.let {
                    viewModelScope.launch {
                        _uiAction.emit(
                            BookDetailsUiAction.NavigateToRoute(
                                route = ReadNavRoute(
                                    bookId = bookId.name,
                                    chapterNumber = event.chapterNumber,
                                    isChapterRead = it.chapters
                                        .find { chapterModel ->
                                            chapterModel.number == event.chapterNumber
                                        }?.isRead ?: return@launch,
                                    isFromBookDetails = true,
                                ),
                            ),
                        )
                    }
                }
            }
        }
    }

    private companion object {
        const val SOURCE_BOOK_DETAILS = "book_details"
    }
}
