package com.quare.bibleplanner.feature.books.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.content_description_clear_search
import bibleplanner.feature.books.generated.resources.content_description_search
import bibleplanner.feature.books.generated.resources.content_description_selected
import bibleplanner.feature.books.generated.resources.content_description_sort
import bibleplanner.feature.books.generated.resources.filter
import bibleplanner.feature.books.generated.resources.new_testament
import bibleplanner.feature.books.generated.resources.old_testament
import bibleplanner.feature.books.generated.resources.reading_not_available_yet
import bibleplanner.feature.books.generated.resources.search_books
import bibleplanner.feature.books.generated.resources.sort_alphabetical_ascending
import bibleplanner.feature.books.generated.resources.sort_alphabetical_descending
import com.quare.bibleplanner.feature.books.presentation.binding.BookGroup
import com.quare.bibleplanner.feature.books.presentation.binding.BookTestament
import com.quare.bibleplanner.feature.books.presentation.component.BookItemComponent
import com.quare.bibleplanner.feature.books.presentation.model.BookFilterOption
import com.quare.bibleplanner.feature.books.presentation.model.BookFilterType
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BookSortOrder
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import com.quare.bibleplanner.ui.utils.LocalMainPadding
import org.jetbrains.compose.resources.stringResource

@Composable
fun BooksScreen(
    state: BooksUiState,
    onEvent: (BooksUiEvent) -> Unit,
) {
    val mainPadding = LocalMainPadding.current
    val lazyListState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0
        }
    }

    Scaffold(
        topBar = {
            BooksTopBar(
                state = state,
                onEvent = onEvent,
                contentPadding = mainPadding,
                isScrolled = isScrolled,
            )
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
                        lazyListState = lazyListState,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BooksTopBar(
    state: BooksUiState,
    onEvent: (BooksUiEvent) -> Unit,
    contentPadding: PaddingValues,
    isScrolled: Boolean,
) {
    Surface(
        shadowElevation = if (isScrolled) 4.dp else 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = contentPadding.calculateTopPadding(), bottom = 8.dp)
                .padding(horizontal = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val query = (state as? BooksUiState.Success)?.searchQuery.orEmpty()
                BooksSearchBar(
                    query = query,
                    onQueryChange = { onEvent(BooksUiEvent.OnSearchQueryChange(it)) },
                    onClearSearch = { onEvent(BooksUiEvent.OnClearSearch) },
                    modifier = Modifier.weight(1f),
                )

                if (state is BooksUiState.Success) {
                    BooksSortMenu(
                        isVisible = state.isSortMenuVisible,
                        currentOrder = state.sortOrder,
                        onToggleMenu = { onEvent(BooksUiEvent.OnToggleSortMenu(it)) },
                        onDismiss = { onEvent(BooksUiEvent.OnDismissSortMenu) },
                        onOrderSelect = { onEvent(BooksUiEvent.OnSortOrderSelect(it)) },
                    )

                    HorizontalSpacer(4.dp)

                    BooksFilterMenu(
                        isVisible = state.isFilterMenuVisible,
                        filterOptions = state.filterOptions,
                        onToggleMenu = { onEvent(BooksUiEvent.OnToggleFilterMenu(it)) },
                        onDismiss = { onEvent(BooksUiEvent.OnDismissFilterMenu) },
                        onFilterToggle = { onEvent(BooksUiEvent.OnToggleFilter(it)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BooksInformationBox(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.reading_not_available_yet),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BooksSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                placeholder = { Text(stringResource(Res.string.search_books)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(Res.string.content_description_search),
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        CommonIconButton(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(
                                Res.string.content_description_clear_search,
                            ),
                            onClick = onClearSearch,
                        )
                    }
                },
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier = modifier,
    ) {}
}

@Composable
private fun BooksSortMenu(
    isVisible: Boolean,
    currentOrder: BookSortOrder?,
    onToggleMenu: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onOrderSelect: (BookSortOrder) -> Unit,
) {
    val isSorted = currentOrder != null
    Box {
        CommonIconButton(
            imageVector = Icons.Default.SortByAlpha,
            contentDescription = stringResource(Res.string.content_description_sort),
            onClick = { onToggleMenu(true) },
            tint = if (isSorted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(40.dp)
                .border(
                    width = 1.dp,
                    color = if (isSorted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    shape = CircleShape,
                ).then(
                    if (isSorted) {
                        Modifier.background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape,
                        )
                    } else {
                        Modifier
                    },
                ),
        )
        DropdownMenu(
            expanded = isVisible,
            onDismissRequest = onDismiss,
        ) {
            BookSortOrder.entries.forEach { order ->
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(
                                when (order) {
                                    BookSortOrder.AlphabeticalAscending -> Res.string.sort_alphabetical_ascending
                                    BookSortOrder.AlphabeticalDescending -> Res.string.sort_alphabetical_descending
                                },
                            ),
                        )
                    },
                    onClick = { onOrderSelect(order) },
                    trailingIcon = if (currentOrder == order) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = stringResource(
                                    Res.string.content_description_selected,
                                ),
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
private fun BooksFilterMenu(
    isVisible: Boolean,
    filterOptions: List<BookFilterOption>,
    onToggleMenu: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onFilterToggle: (BookFilterType) -> Unit,
) {
    val isFiltered = filterOptions.any { it.isSelected }
    Box {
        CommonIconButton(
            imageVector = Icons.Default.FilterList,
            contentDescription = stringResource(Res.string.filter),
            onClick = { onToggleMenu(true) },
            tint = if (isFiltered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(40.dp)
                .border(
                    width = 1.dp,
                    color = if (isFiltered) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    shape = CircleShape,
                ).then(
                    if (isFiltered) {
                        Modifier.background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape,
                        )
                    } else {
                        Modifier
                    },
                ),
        )
        DropdownMenu(
            expanded = isVisible,
            onDismissRequest = onDismiss,
        ) {
            filterOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.label)) },
                    onClick = { onFilterToggle(option.type) },
                    trailingIcon = if (option.isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(
                                    Res.string.content_description_selected,
                                ),
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookList(
    state: BooksUiState.Success,
    onEvent: (BooksUiEvent) -> Unit,
    lazyListState: LazyListState,
) {
    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            BooksInformationBox()
        }

        if (state.shouldShowTestamentToggle) {
            item {
                TestamentToggle(
                    selectedTestament = state.selectedTestament,
                    onTestamentSelect = { onEvent(BooksUiEvent.OnTestamentSelect(it)) },
                )
            }
        }

        if (!state.shouldShowTestamentToggle) {
            items(state.filteredBooks) { book ->
                BookItem(
                    book = book,
                    onEvent = onEvent,
                )
            }
        } else {
            state.groupsInTestament.forEach { presentationGroup ->
                item {
                    BookGroupHeader(presentationGroup.group)
                }

                items(presentationGroup.books) { book ->
                    BookItem(
                        book = book,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@Composable
private fun TestamentToggle(
    selectedTestament: BookTestament,
    onTestamentSelect: (BookTestament) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        val testaments = listOf(
            BookTestament.OldTestament,
            BookTestament.NewTestament,
        )
        testaments.forEachIndexed { index, testament ->
            SegmentedButton(
                selected = selectedTestament == testament,
                onClick = { onTestamentSelect(testament) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = testaments.size,
                ),
            ) {
                Text(
                    text = stringResource(
                        when (testament) {
                            BookTestament.OldTestament -> Res.string.old_testament
                            BookTestament.NewTestament -> Res.string.new_testament
                        },
                    ),
                )
            }
        }
    }
}

@Composable
private fun BookItem(
    book: BookPresentationModel,
    onEvent: (BooksUiEvent) -> Unit,
) {
    BookItemComponent(
        book = book,
        onClick = { onEvent(BooksUiEvent.OnBookClick(book)) },
        onToggleFavorite = { onEvent(BooksUiEvent.OnToggleFavorite(book.id)) },
    )
}

@Composable
private fun BookGroupHeader(group: BookGroup) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Line()
        Text(
            text = stringResource(group.titleRes),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Line()
    }
}

@Composable
private fun RowScope.Line() {
    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.LightGray))
}
