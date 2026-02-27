package com.quare.bibleplanner.feature.bookdetails.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.books.domain.usecase.GetBookByIdFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetBooksFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdateBookReadStatusUseCase
import com.quare.bibleplanner.core.books.presentation.mapper.BookGroupMapper
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.route.BookDetailsNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiAction
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiEvent
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiState
import com.quare.bibleplanner.feature.bookdetails.presentation.utils.toSynopsisResource
import com.quare.bibleplanner.ui.utils.observe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class BookDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val booksRepository: BooksRepository,
    private val bookGroupMapper: BookGroupMapper,
    private val markBookRead: UpdateBookReadStatusUseCase,
    getBookByIdFlow: GetBookByIdFlowUseCase,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<BookDetailsNavRoute>()
    private val bookId = BookId.valueOf(route.bookId)

    private val _uiState = MutableStateFlow<BookDetailsUiState>(BookDetailsUiState.Loading)
    val uiState: StateFlow<BookDetailsUiState> = _uiState

    private val _uiAction = MutableSharedFlow<BookDetailsUiAction>()
    val uiAction: SharedFlow<BookDetailsUiAction> = _uiAction

    private val successState get() = uiState.value as? BookDetailsUiState.Success

    init {
        observe(getBookByIdFlow(bookId)) { fetchedBook ->
            val book = fetchedBook ?: return@observe
            val totalChapters = book.chapters.size
            val readChapters = book.chapters.count { it.isRead }
            val progress = if (totalChapters > 0) readChapters.toFloat() / totalChapters else 0f

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
                    areAllChaptersRead = readChapters == totalChapters,
                    isFavorite = book.isFavorite,
                    bookGroup = bookGroup,
                    bookCategoryName = bookCategoryName,
                    isSynopsisExpanded = isSynopsisExpanded,
                )
            }
        }
    }

    fun onEvent(event: BookDetailsUiEvent) {
        when (event) {
            BookDetailsUiEvent.OnBackClick -> {
                viewModelScope.launch {
                    _uiAction.emit(BookDetailsUiAction.NavigateBack)
                }
            }

            BookDetailsUiEvent.OnToggleFavorite -> {
                successState?.let {
                    viewModelScope.launch {
                        booksRepository.updateBookFavoriteStatus(
                            bookId = bookId,
                            isFavorite = !it.isFavorite,
                        )
                    }
                }
            }

            BookDetailsUiEvent.OnToggleSynopsisExpanded -> {
                _uiState.update {
                    (it as? BookDetailsUiState.Success)?.copy(
                        isSynopsisExpanded = !it.isSynopsisExpanded,
                    ) ?: it
                }
            }

            BookDetailsUiEvent.OnToggleAllChapters -> {
                successState?.let {
                    viewModelScope.launch {
                        markBookRead(bookId = bookId, isRead = !it.areAllChaptersRead)
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
}
