package com.quare.bibleplanner.feature.books.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.favorites
import bibleplanner.feature.books.generated.resources.only_read
import bibleplanner.feature.books.generated.resources.only_unread
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.feature.books.presentation.binding.BookTestament
import com.quare.bibleplanner.feature.books.presentation.mapper.BookCategorizationMapper
import com.quare.bibleplanner.feature.books.presentation.mapper.BookGroupMapper
import com.quare.bibleplanner.feature.books.presentation.model.BookFilterOption
import com.quare.bibleplanner.feature.books.presentation.model.BookFilterType
import com.quare.bibleplanner.feature.books.presentation.model.BookGroupPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BookSortOrder
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BooksViewModel(
    private val booksRepository: BooksRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<BooksUiState>(BooksUiState.Loading)
    val uiState: StateFlow<BooksUiState> = _uiState

    private val bookGroupMapper = BookGroupMapper()
    private val bookCategorizationMapper = BookCategorizationMapper(bookGroupMapper)
    private var allBooks: List<BookDataModel> = emptyList()

    init {
        viewModelScope.launch {
            booksRepository.getBooksFlow().collect { books ->
                allBooks = books
                updateState()
            }
        }
    }

    // Temporary local storage for favorites until data layer is ready
    private val favoriteBookIds = mutableSetOf<com.quare.bibleplanner.core.model.book.BookId>()

    private var activeFilterTypes: Set<BookFilterType> = emptySet()
    private var isFilterMenuVisible: Boolean = false
    private var isSortMenuVisible: Boolean = false
    private var sortOrder: BookSortOrder? = null

    fun onEvent(event: BooksUiEvent) {
        when (event) {
            is BooksUiEvent.OnSearchQueryChange -> {
                updateState(searchQuery = event.query)
            }

            is BooksUiEvent.OnTestamentSelect -> {
                updateState(selectedTestament = event.testament)
            }

            is BooksUiEvent.OnBookClick -> {
                // No-op for now
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
            }

            is BooksUiEvent.OnToggleFavorite -> {
                if (favoriteBookIds.contains(event.bookId)) {
                    favoriteBookIds.remove(event.bookId)
                } else {
                    favoriteBookIds.add(event.bookId)
                }
                updateState() // Re-calculate presentation models
            }

            is BooksUiEvent.OnToggleFilterMenu -> {
                isFilterMenuVisible = event.isVisible
                updateState()
            }

            is BooksUiEvent.OnToggleSortMenu -> {
                isSortMenuVisible = event.isVisible
                updateState()
            }

            is BooksUiEvent.OnSortOrderSelect -> {
                sortOrder = if (sortOrder == event.sortOrder) null else event.sortOrder
                isSortMenuVisible = false
                updateState()
            }

            is BooksUiEvent.OnDismissSortMenu -> {
                isSortMenuVisible = false
                updateState()
            }

            is BooksUiEvent.OnDismissFilterMenu -> {
                isFilterMenuVisible = false
                updateState()
            }

            is BooksUiEvent.OnClearSearch -> {
                updateState(searchQuery = "")
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
                isFavorite = favoriteBookIds.contains(book.id),
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
                label = Res.string.only_read,
                isSelected = isOnlyRead,
            ),
            BookFilterOption(
                type = BookFilterType.OnlyUnread,
                label = Res.string.only_unread,
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
                groupsInTestament = categorizedBooks[currentSelectedTestament].orEmpty(),
            )
        }
    }
}
