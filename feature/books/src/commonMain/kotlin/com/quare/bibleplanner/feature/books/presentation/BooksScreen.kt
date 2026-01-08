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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.content_description_clear_search
import bibleplanner.feature.books.generated.resources.content_description_search
import bibleplanner.feature.books.generated.resources.content_description_selected
import bibleplanner.feature.books.generated.resources.filter
import bibleplanner.feature.books.generated.resources.new_testament
import bibleplanner.feature.books.generated.resources.old_testament
import bibleplanner.feature.books.generated.resources.search_books
import com.quare.bibleplanner.feature.books.presentation.binding.BookGroup
import com.quare.bibleplanner.feature.books.presentation.binding.BookTestament
import com.quare.bibleplanner.feature.books.presentation.component.BookItemComponent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import com.quare.bibleplanner.ui.utils.LocalMainPadding
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BooksScreen(
    state: BooksUiState,
    onEvent: (BooksUiEvent) -> Unit,
) {
    val mainPadding = LocalMainPadding.current
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = mainPadding.calculateTopPadding(), bottom = 8.dp)
                    .padding(horizontal = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DockedSearchBar(
                        inputField = {
                            val query = (state as? BooksUiState.Success)?.searchQuery.orEmpty()
                            SearchBarDefaults.InputField(
                                query = query,
                                onQueryChange = { onEvent(BooksUiEvent.OnSearchQueryChange(it)) },
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
                                            onClick = { onEvent(BooksUiEvent.OnClearSearch) },
                                        )
                                    }
                                },
                            )
                        },
                        expanded = false,
                        onExpandedChange = {},
                        modifier = Modifier.weight(1f),
                    ) {}

                    if (state is BooksUiState.Success) {
                        Box {
                            CommonIconButton(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = stringResource(Res.string.filter),
                                onClick = { onEvent(BooksUiEvent.OnToggleFilterMenu(true)) },
                            )
                            DropdownMenu(
                                expanded = state.isFilterMenuVisible,
                                onDismissRequest = { onEvent(BooksUiEvent.OnToggleFilterMenu(false)) },
                            ) {
                                state.filterOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(option.label)) },
                                        onClick = {
                                            onEvent(BooksUiEvent.OnToggleFilter(option.type))
                                            onEvent(BooksUiEvent.OnToggleFilterMenu(false))
                                        },
                                        trailingIcon = if (option.isSelected) {
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
        item {
            if (state.shouldShowTestamentToggle) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val testaments = listOf(
                        BookTestament.OldTestament,
                        BookTestament.NewTestament,
                    )
                    testaments.forEachIndexed { index, testament ->
                        SegmentedButton(
                            selected = state.selectedTestament == testament,
                            onClick = { onEvent(BooksUiEvent.OnTestamentSelect(testament)) },
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
                VerticalSpacer(8.dp)
            }
        }
        if (!state.shouldShowTestamentToggle) {
            items(state.filteredBooks) { book ->
                BookItemComponent(
                    book = book,
                    onClick = { onEvent(BooksUiEvent.OnBookClick(book)) },
                    onToggleFavorite = { onEvent(BooksUiEvent.OnToggleFavorite(book.id)) },
                )
            }
        } else {
            state.groupsInTestament.forEach { presentationGroup ->
                item {
                    BookGroupHeader(presentationGroup.group)
                }

                items(presentationGroup.books) { book ->
                    BookItemComponent(
                        book = book,
                        onClick = { onEvent(BooksUiEvent.OnBookClick(book)) },
                        onToggleFavorite = { onEvent(BooksUiEvent.OnToggleFavorite(book.id)) },
                    )
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
