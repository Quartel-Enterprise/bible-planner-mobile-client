package com.quare.bibleplanner.feature.read.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.quare.bibleplanner.core.books.domain.usecase.ToggleWholeChapterReadStatusUseCase
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.presentation.factory.ReadDataPresentationModelFactory
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiAction
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.ui.utils.observe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReadViewModel(
    savedStateHandle: SavedStateHandle,
    private val readDataPresentationModelFactory: ReadDataPresentationModelFactory,
    private val toggleWholeChapterReadStatus: ToggleWholeChapterReadStatusUseCase,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<ReadNavRoute>()
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

    fun onEvent(event: ReadUiEvent) {
        when (event) {
            ReadUiEvent.OnArrowBackClick -> {
                navigateBack()
            }

            ReadUiEvent.OnRetryClick -> {
                loadChapterContent()
            }

            is ReadUiEvent.ToggleReadStatus -> {
                viewModelScope.launch {
                    toggleWholeChapterReadStatus(
                        bookId = bookId,
                        chapterNumber = chapterNumber,
                    )
                }
            }

            ReadUiEvent.ManageBibleVersions -> {
                viewModelScope.launch {
                    _uiAction.emit(ReadUiAction.NavigateToRoute(BibleVersionSelectorRoute))
                }
            }

            is ReadUiEvent.OnNavigationSuggestionClick -> {
                val suggestion = event.suggestion
                viewModelScope.launch {
                    _uiAction.emit(
                        ReadUiAction.NavigateToRoute(
                            ReadNavRoute(
                                bookId = suggestion.bookId.name,
                                chapterNumber = suggestion.chapterNumber,
                                isChapterRead = uiState.value.isChapterRead,
                                isFromBookDetails = route.isFromBookDetails,
                            )
                        )
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
        )
    )
}
