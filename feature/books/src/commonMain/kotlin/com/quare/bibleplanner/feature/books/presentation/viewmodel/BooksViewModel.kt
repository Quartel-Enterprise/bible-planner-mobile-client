package com.quare.bibleplanner.feature.books.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.favorites
import bibleplanner.feature.books.generated.resources.read
import bibleplanner.feature.books.generated.resources.unread
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.books.domain.usecase.GetBooksWithInformationBoxVisibilityUseCase
import com.quare.bibleplanner.core.books.domain.usecase.ToggleBookFavoriteUseCase
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.GetWebAppUrl
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.IsMoreWebAppEnabled
import com.quare.bibleplanner.feature.books.presentation.binding.BookTestament
import com.quare.bibleplanner.feature.books.presentation.mapper.BookCategorizationMapper
import com.quare.bibleplanner.feature.books.presentation.mapper.BookGroupMapper
import com.quare.bibleplanner.feature.books.presentation.model.BookFilterOption
import com.quare.bibleplanner.feature.books.presentation.model.BookFilterType
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BookSortOrder
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiAction
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.ui.utils.observe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BooksViewModel(
    private val booksRepository: BooksRepository,
    private val toggleBookFavorite: ToggleBookFavoriteUseCase,
    getBooksWithInformationBoxVisibility: GetBooksWithInformationBoxVisibilityUseCase,
    private val getWebAppUrl: GetWebAppUrl,
    private val isMoreWebAppEnabled: IsMoreWebAppEnabled,
) : ViewModel() {
    private val _uiState = MutableStateFlow<BooksUiState>(BooksUiState.Loading)
    val uiState: StateFlow<BooksUiState> = _uiState

    private val _uiAction = MutableSharedFlow<BooksUiAction>()
    val uiAction: SharedFlow<BooksUiAction> = _uiAction.asSharedFlow()

    private val bookGroupMapper = BookGroupMapper()
    private val bookCategorizationMapper = BookCategorizationMapper(bookGroupMapper)
    private var allBooks: List<BookDataModel> = emptyList()
    private var isInformationBoxVisible: Boolean = true

    init {
        observe(getBooksWithInformationBoxVisibility()) { result ->
            allBooks = result.books
            isInformationBoxVisible = result.isInformationBoxVisible
            updateState()
        }
    }

    private var activeFilterTypes: Set<BookFilterType> = emptySet()
    private var isFilterMenuVisible: Boolean = false
    private var isSortMenuVisible: Boolean = false
    private var sortOrder: BookSortOrder? = null
    private var layoutFormat: BookLayoutFormat = BookLayoutFormat.List

    fun onEvent(event: BooksUiEvent) {
        when (event) {
            is BooksUiEvent.OnSearchQueryChange -> {
                updateState(searchQuery = event.query)
                viewModelScope.launch {
                    _uiAction.emit(BooksUiAction.ScrollToTop)
                }
            }

            is BooksUiEvent.OnTestamentSelect -> {
                updateState(selectedTestament = event.testament)
                viewModelScope.launch {
                    _uiAction.emit(BooksUiAction.ScrollToTop)
                }
            }

            is BooksUiEvent.OnBookClick -> {
                viewModelScope.launch {
                    if (isMoreWebAppEnabled()) {
                        _uiAction.emit(BooksUiAction.ShowReadingNotAvailableYetSnackbar(getWebAppUrl()))
                    }
                }
            }

            is BooksUiEvent.OnToggleFilter -> {
                activeFilterTypes = when (event.filterType) {
                    BookFilterType.OnlyRead -> {
                        if (activeFilterTypes.contains(BookFilterType.OnlyRead)) {
                            activeFilterTypes - BookFilterType.OnlyRead
                        } else {
                            (activeFilterTypes - BookFilterType.OnlyUnread) + BookFilterType.OnlyRead
                        }
                    }

                    BookFilterType.OnlyUnread -> {
                        if (activeFilterTypes.contains(BookFilterType.OnlyUnread)) {
                            activeFilterTypes - BookFilterType.OnlyUnread
                        } else {
                            (activeFilterTypes - BookFilterType.OnlyRead) + BookFilterType.OnlyUnread
                        }
                    }

                    BookFilterType.Favorites -> {
                        if (activeFilterTypes.contains(BookFilterType.Favorites)) {
                            activeFilterTypes - BookFilterType.Favorites
                        } else {
                            activeFilterTypes + BookFilterType.Favorites
                        }
                    }
                }
                isFilterMenuVisible = false
                updateState()
                viewModelScope.launch {
                    _uiAction.emit(BooksUiAction.ScrollToTop)
                }
            }

            is BooksUiEvent.OnToggleFavorite -> {
                val book = (uiState.value as? BooksUiState.Success)?.books?.find { it.id == event.bookId }
                book?.let {
                    viewModelScope.launch {
                        toggleBookFavorite(event.bookId, !it.isFavorite)
                    }
                }
            }

            is BooksUiEvent.OnToggleFilterMenu -> {
                isFilterMenuVisible = !isFilterMenuVisible
                updateState()
            }

            is BooksUiEvent.OnToggleSortMenu -> {
                isSortMenuVisible = !isSortMenuVisible
                updateState()
            }

            is BooksUiEvent.OnSortOrderSelect -> {
                sortOrder = if (sortOrder == event.sortOrder) null else event.sortOrder
                isSortMenuVisible = false
                updateState()
                viewModelScope.launch {
                    _uiAction.emit(BooksUiAction.ScrollToTop)
                }
            }

            is BooksUiEvent.OnDismissSortMenu -> {
                isSortMenuVisible = false
                updateState()
            }

            is BooksUiEvent.OnDismissFilterMenu -> {
                isFilterMenuVisible = false
                updateState()
            }

            is BooksUiEvent.OnDismissInformationBox -> {
                viewModelScope.launch {
                    booksRepository.setInformationBoxDismissed()
                }
            }

            is BooksUiEvent.OnClearSearch -> {
                updateState(searchQuery = "")
            }

            is BooksUiEvent.OnLayoutFormatSelect -> {
                layoutFormat = event.layoutFormat
                updateState()
                viewModelScope.launch {
                    _uiAction.emit(BooksUiAction.ScrollToTop)
                }
            }

            BooksUiEvent.OnWebAppLinkClick -> {
                viewModelScope.launch {
                    _uiAction.emit(BooksUiAction.OpenWebAppLink(getWebAppUrl()))
                }
            }
        }
    }

    private fun updateState(
        searchQuery: String? = null,
        selectedTestament: BookTestament? = null,
    ) {
        val currentState = _uiState.value
        val currentQuery = searchQuery ?: (currentState as? BooksUiState.Success)?.searchQuery.orEmpty()
        val currentSelectedTestament = selectedTestament
            ?: (currentState as? BooksUiState.Success)?.selectedTestament
            ?: BookTestament.OldTestament

        val isOnlyRead = activeFilterTypes.contains(BookFilterType.OnlyRead)
        val isOnlyUnread = activeFilterTypes.contains(BookFilterType.OnlyUnread)
        val isFavoritesOnly = activeFilterTypes.contains(BookFilterType.Favorites)

        val presentationModels = allBooks.map { book ->
            val totalChapters = book.chapters.size
            val readChapters = book.chapters.count { it.isRead }
            val progress = if (totalChapters > 0) readChapters.toFloat() / totalChapters else 0f
            val percentage = (progress * 100).toInt()
            val isCompleted = progress >= 1f

            BookPresentationModel(
                id = book.id,
                name = book.id.name, // Use enum name for internal logic/filtering for now to avoid Composable call
                chapterProgressText = "$readChapters/$totalChapters",
                progress = progress,
                percentageText = "$percentage%",
                isCompleted = isCompleted,
                isFavorite = book.isFavorite,
            )
        }

        val isSearchFilterOrSortActive = currentQuery.isNotBlank() ||
            activeFilterTypes.isNotEmpty() ||
            sortOrder != null

        val filtered = presentationModels.filter { book ->
            val matchesQuery = if (currentQuery.isBlank()) true else book.name.contains(currentQuery, ignoreCase = true)
            val matchesOnlyRead = if (isOnlyRead) book.isCompleted else true
            val matchesOnlyUnread = if (isOnlyUnread) !book.isCompleted else true
            val matchesFavorites = if (isFavoritesOnly) book.isFavorite else true
            val matchesTestament = if (isSearchFilterOrSortActive) {
                true
            } else {
                bookGroupMapper.fromBookId(book.id).testament ==
                    currentSelectedTestament
            }

            matchesQuery && matchesOnlyRead && matchesOnlyUnread && matchesFavorites && matchesTestament
        }

        val filterOptions = listOf(
            BookFilterOption(
                type = BookFilterType.OnlyRead,
                label = Res.string.read,
                isSelected = isOnlyRead,
            ),
            BookFilterOption(
                type = BookFilterType.OnlyUnread,
                label = Res.string.unread,
                isSelected = isOnlyUnread,
            ),
            BookFilterOption(
                type = BookFilterType.Favorites,
                label = Res.string.favorites,
                isSelected = isFavoritesOnly,
            ),
        )

        val categorizedBooks = bookCategorizationMapper
            .map(filtered)
            .mapValues { (_, groups) ->
                groups.map { group ->
                    group.copy(
                        books = when (sortOrder) {
                            BookSortOrder.AlphabeticalAscending -> group.books.sortedBy { it.id.name }
                            BookSortOrder.AlphabeticalDescending -> group.books.sortedByDescending { it.id.name }
                            null -> group.books
                        },
                    )
                }
            }

        _uiState.update {
            BooksUiState.Success(
                books = presentationModels,
                filteredBooks = when (sortOrder) {
                    BookSortOrder.AlphabeticalAscending -> filtered.sortedBy { it.id.name }
                    BookSortOrder.AlphabeticalDescending -> filtered.sortedByDescending { it.id.name }
                    null -> filtered
                },
                selectedTestament = currentSelectedTestament,
                searchQuery = currentQuery,
                filterOptions = filterOptions,
                shouldShowTestamentToggle = !isSearchFilterOrSortActive,
                isFilterMenuVisible = isFilterMenuVisible,
                isSortMenuVisible = isSortMenuVisible,
                sortOrder = sortOrder,
                isInformationBoxVisible = isInformationBoxVisible,
                groupsInTestament = categorizedBooks[currentSelectedTestament].orEmpty(),
                layoutFormat = layoutFormat,
            )
        }
    }
}
