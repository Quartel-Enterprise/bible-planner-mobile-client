package com.quare.bibleplanner.feature.books.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.feature.books.presentation.mapper.BookGroupMapper
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
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

    fun onEvent(event: BooksUiEvent) {
        when (event) {
            is BooksUiEvent.OnSearchQueryChange -> {
                updateState(searchQuery = event.query)
            }

            is BooksUiEvent.OnToggleCategorize -> {
                val current = (_uiState.value as? BooksUiState.Success)?.isCategorized ?: false
                updateState(isCategorized = !current)
            }

            is BooksUiEvent.OnToggleGroupExpansion -> {
                updateSuccessState { currentState ->
                    val currentExpanded = currentState.expandedGroups
                    val newExpanded = if (currentExpanded.contains(event.groupName)) {
                        currentExpanded - event.groupName
                    } else {
                        currentExpanded + event.groupName
                    }
                    currentState.copy(expandedGroups = newExpanded)
                }
            }

            is BooksUiEvent.OnBookClick -> {
                // No-op for now
            }

            is BooksUiEvent.OnToggleOnlyRead -> {
                val current = (_uiState.value as? BooksUiState.Success)?.isOnlyRead ?: false
                updateState(isOnlyRead = !current)
            }

            is BooksUiEvent.OnToggleFavorites -> {
                val current = (_uiState.value as? BooksUiState.Success)?.isFavoritesOnly ?: false
                updateState(isFavoritesOnly = !current)
            }

            is BooksUiEvent.OnToggleFavorite -> {
                if (favoriteBookIds.contains(event.bookId)) {
                    favoriteBookIds.remove(event.bookId)
                } else {
                    favoriteBookIds.add(event.bookId)
                }
                updateState() // Re-calculate presentation models
            }
        }
    }

    private fun updateSuccessState(transform: (BooksUiState.Success) -> BooksUiState.Success) {
        _uiState.update { currentState ->
            if (currentState is BooksUiState.Success) {
                transform(currentState)
            } else {
                currentState
            }
        }
    }

    private fun updateState(
        searchQuery: String? = null,
        isCategorized: Boolean? = null,
        expandedGroups: Set<String>? = null,
        isOnlyRead: Boolean? = null,
        isFavoritesOnly: Boolean? = null,
    ) {
        val currentState = _uiState.value
        val currentQuery = searchQuery ?: (currentState as? BooksUiState.Success)?.searchQuery ?: ""
        val currentIsCategorized = isCategorized ?: (currentState as? BooksUiState.Success)?.isCategorized ?: false
        val currentExpanded = expandedGroups ?: (currentState as? BooksUiState.Success)?.expandedGroups ?: emptySet()
        val currentIsOnlyRead = isOnlyRead ?: (currentState as? BooksUiState.Success)?.isOnlyRead ?: false
        val currentIsFavoritesOnly =
            isFavoritesOnly ?: (currentState as? BooksUiState.Success)?.isFavoritesOnly ?: false

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

        val filtered = presentationModels.filter { book ->
            val matchesQuery = if (currentQuery.isBlank()) true else book.name.contains(currentQuery, ignoreCase = true)
            val matchesOnlyRead = if (currentIsOnlyRead) book.isCompleted else true
            val matchesFavorites = if (currentIsFavoritesOnly) book.isFavorite else true

            matchesQuery && matchesOnlyRead && matchesFavorites
        }

        _uiState.update {
            BooksUiState.Success(
                books = presentationModels,
                filteredBooks = filtered,
                isCategorized = currentIsCategorized,
                searchQuery = currentQuery,
                expandedGroups = currentExpanded,
                isOnlyRead = currentIsOnlyRead,
                isFavoritesOnly = currentIsFavoritesOnly,
                categorizedBooks = if (currentIsCategorized) {
                    val groupedBooks = filtered.groupBy { bookGroupMapper.fromBookId(it.id) }
                    groupedBooks.keys
                        .groupBy { it.testament }
                        .mapValues { (_, groups) ->
                            groups.associateWith { group ->
                                groupedBooks[group] ?: emptyList()
                            }
                        }
                } else {
                    emptyMap()
                },
            )
        }
    }
}
