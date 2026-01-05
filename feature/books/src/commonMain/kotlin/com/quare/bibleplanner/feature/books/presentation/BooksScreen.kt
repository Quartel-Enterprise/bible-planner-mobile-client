package com.quare.bibleplanner.feature.books.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.categorize
import bibleplanner.feature.books.generated.resources.favorites
import bibleplanner.feature.books.generated.resources.new_testament
import bibleplanner.feature.books.generated.resources.old_testament
import bibleplanner.feature.books.generated.resources.only_read
import bibleplanner.feature.books.generated.resources.search_books
import bibleplanner.feature.books.generated.resources.show_less
import bibleplanner.feature.books.generated.resources.show_more
import com.quare.bibleplanner.feature.books.presentation.binding.BookGroup
import com.quare.bibleplanner.feature.books.presentation.binding.BookTestament
import com.quare.bibleplanner.feature.books.presentation.component.BookItemComponent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BooksScreen(
    state: BooksUiState,
    onEvent: (BooksUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = (state as? BooksUiState.Success)?.searchQuery ?: "",
                            onQueryChange = { onEvent(BooksUiEvent.OnSearchQueryChange(it)) },
                            onSearch = {},
                            expanded = false,
                            onExpandedChange = {},
                            placeholder = { Text(stringResource(Res.string.search_books)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        )
                    },
                    expanded = false,
                    onExpandedChange = {},
                    modifier = Modifier.fillMaxWidth(),
                ) {}

                if (state is BooksUiState.Success) {
                    VerticalSpacer(8.dp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = state.isCategorized,
                            onClick = { onEvent(BooksUiEvent.OnToggleCategorize) },
                            label = { Text(stringResource(Res.string.categorize)) },
                            leadingIcon = if (state.isCategorized) {
                                { Icon(Icons.Default.ExpandLess, contentDescription = null) }
                            } else {
                                null
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                        // Add "Only Read" and "Favorites" placeholders as per design, but non-functional for now if not requested?
                        // "options after the search bar will be 'Categrize' ... if not selected we'll just display the simple list"
                        // User mentioned screenshot options. I'll add them as static for now to match design.
                        FilterChip(
                            selected = state.isOnlyRead,
                            onClick = { onEvent(BooksUiEvent.OnToggleOnlyRead) },
                            label = { Text(stringResource(Res.string.only_read)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                        FilterChip(
                            selected = state.isFavoritesOnly,
                            onClick = { onEvent(BooksUiEvent.OnToggleFavorites) },
                            label = { Text(stringResource(Res.string.favorites)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (state) {
                is BooksUiState.Loading -> {
                    // Loading indicator
                }

                is BooksUiState.Success -> {
                    BookList(
                        state = state,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookList(
    state: BooksUiState.Success,
    onEvent: (BooksUiEvent) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (!state.isCategorized) {
            items(state.filteredBooks) { book ->
                BookItemComponent(
                    book = book,
                    onClick = { onEvent(BooksUiEvent.OnBookClick(book)) },
                    onToggleFavorite = { onEvent(BooksUiEvent.OnToggleFavorite(book.id)) },
                )
            }
        } else {
            // Group books logic moved to ViewModel
            val categorizedBooks = state.categorizedBooks

            // Order: Old Testament, then New Testament
            val testaments = listOf(BookTestament.OldTestament, BookTestament.NewTestament)

            testaments.forEach { testament ->
                val groupsInTestament = categorizedBooks[testament]
                if (!groupsInTestament.isNullOrEmpty()) {
                    stickyHeader {
                        Text(
                            text = when (testament) {
                                BookTestament.OldTestament -> stringResource(Res.string.old_testament)
                                BookTestament.NewTestament -> stringResource(Res.string.new_testament)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(vertical = 8.dp),
                        )
                    }

                    groupsInTestament.keys
                        .sortedBy {
                            // Simple order fix: defined in enum ordinal but here we have a list.
                            // Enum ordinal is default sort, which matches Bible order.
                            (it as? Enum<*>)?.ordinal ?: 0
                        }.forEach { group ->
                            item {
                                BookGroupHeader(group)
                            }

                            val booksInGroup = groupsInTestament[group] ?: emptyList()
                            val isExpanded = state.expandedGroups.contains(group.toString())
                            val displayBooks = if (isExpanded) booksInGroup else booksInGroup.take(2)

                            items(displayBooks) { book ->
                                BookItemComponent(
                                    book = book,
                                    onClick = { onEvent(BooksUiEvent.OnBookClick(book)) },
                                    onToggleFavorite = { onEvent(BooksUiEvent.OnToggleFavorite(book.id)) },
                                )
                            }

                            if (booksInGroup.size > 2) {
                                item {
                                    BookGroupFooter(
                                        isExpanded = isExpanded,
                                        onToggle = { onEvent(BooksUiEvent.OnToggleGroupExpansion(group.toString())) },
                                    )
                                }
                            }
                        }
                }
            }
        }
    }
}

@Composable
private fun BookGroupHeader(group: BookGroup) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Line
        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.LightGray))
        Text(
            text = stringResource(group.titleRes),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.LightGray))
    }
}

@Composable
private fun BookGroupFooter(
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    OutlinedButton(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
    ) {
        val textRes = if (isExpanded) Res.string.show_less else Res.string.show_more
        val icon = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore
        Text(stringResource(textRes))
        HorizontalSpacer(8.dp)
        Icon(
            imageVector = icon,
            contentDescription = null,
        )
    }
}
