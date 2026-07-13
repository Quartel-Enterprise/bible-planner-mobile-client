package com.quare.bibleplanner.feature.read.presentation

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.usecase.ToggleWholeChapterReadStatusUseCase
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.loginnudge.domain.usecase.RequestLoginNudgeIfNeeded
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.presentation.factory.ReadDataPresentationModelFactory
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiAction
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.ui.utils.observe
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReadViewModel(
    private val route: ReadNavRoute,
    private val readDataPresentationModelFactory: ReadDataPresentationModelFactory,
    private val toggleWholeChapterReadStatus: ToggleWholeChapterReadStatusUseCase,
    private val requestLoginNudgeIfNeeded: RequestLoginNudgeIfNeeded,
    trackEvent: TrackEvent,
    val platform: Platform,
) : TrackedViewModel<ReadUiEvent>(trackEvent) {
    private val chapterNumber = route.chapterNumber
    private val isChapterRead = route.isChapterRead
    private val bookId = BookId.valueOf(route.bookId)
    private val bookStringResource = bookId.toBookNameResource()
    private val loadingState = route.toLoadingState()

    private val _uiState = MutableStateFlow<ReadUiState>(loadingState)
    val uiState: StateFlow<ReadUiState> = _uiState

    private val _uiAction = MutableSharedFlow<ReadUiAction>()
    val uiAction: SharedFlow<ReadUiAction> = _uiAction

    init {
        loadChapterContent()
    }

    override fun handleEvent(event: ReadUiEvent) {
        when (event) {
            ReadUiEvent.OnArrowBackClick -> navigateBack()

            ReadUiEvent.OnRetryClick -> loadChapterContent()

            is ReadUiEvent.ToggleReadStatus -> {
                viewModelScope.launch {
                    val isRead = toggleWholeChapterReadStatus(
                        bookId = bookId,
                        chapterNumber = chapterNumber,
                    )
                    trackEvent(
                        name = AnalyticsEventNames.CHAPTER_READ_TOGGLED,
                        params = mapOf(
                            AnalyticsParams.BOOK_ID to bookId.name.lowercase(),
                            AnalyticsParams.CHAPTER_NUMBER to chapterNumber,
                            AnalyticsParams.IS_READ to isRead,
                            AnalyticsParams.SOURCE to SOURCE_READER,
                        ),
                    )
                    requestLoginNudgeIfNeeded()
                }
            }

            ReadUiEvent.ManageBibleVersions -> {
                viewModelScope.launch {
                    _uiAction.emit(
                        ReadUiAction.NavigateToRoute(
                            route = BibleVersionSelectorRoute,
                            replace = false,
                        ),
                    )
                }
            }

            is ReadUiEvent.OnNavigationSuggestionClick -> {
                val suggestion = event.suggestion
                trackEvent(
                    name = AnalyticsEventNames.READING_SUGGESTION_CLICKED,
                    params = mapOf(
                        AnalyticsParams.DIRECTION to suggestion.toDirection(),
                        AnalyticsParams.BOOK_ID to suggestion.bookId.name.lowercase(),
                        AnalyticsParams.CHAPTER_NUMBER to suggestion.chapterNumber,
                    ),
                )
                viewModelScope.launch {
                    _uiAction.emit(
                        ReadUiAction.NavigateToRoute(
                            route = ReadNavRoute(
                                bookId = suggestion.bookId.name,
                                chapterNumber = suggestion.chapterNumber,
                                isChapterRead = uiState.value.isChapterRead,
                                isFromBookDetails = route.isFromBookDetails,
                            ),
                            replace = true,
                        ),
                    )
                }
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _uiAction.emit(ReadUiAction.NavigateBack)
        }
    }

    private fun loadChapterContent() {
        _uiState.update { loadingState }
        observe(
            readDataPresentationModelFactory.create(
                bookId = bookId,
                chapterNumber = chapterNumber,
                bookStringResource = bookStringResource,
                isInitiallyRead = isChapterRead,
                isFromBookDetails = route.isFromBookDetails,
            ),
        ) { state ->
            _uiState.update { state }
        }
    }

    private fun ReadNavRoute.toLoadingState(): ReadUiState.Loading = ReadUiState.Loading(
        chapterNumber = chapterNumber,
        bookStringResource = bookStringResource,
        isChapterRead = isChapterRead,
        navigationSuggestions = ReadNavigationSuggestionsModel(
            previous = null,
            next = null,
        ),
    )

    private fun ReadNavigationSuggestionModel.toDirection(): String =
        if (this == uiState.value.navigationSuggestions.previous) DIRECTION_PREVIOUS else DIRECTION_NEXT

    private companion object {
        const val SOURCE_READER = "reader"
        const val DIRECTION_PREVIOUS = "previous"
        const val DIRECTION_NEXT = "next"
    }
}
