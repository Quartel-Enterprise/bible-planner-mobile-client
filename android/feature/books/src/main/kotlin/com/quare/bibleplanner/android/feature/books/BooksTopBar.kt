package com.quare.bibleplanner.android.feature.books

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.books.presentation.model.BookFilterType
import com.quare.bibleplanner.feature.books.presentation.model.BookSortOrder
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState

private val topBarMaxWidth = 800.dp
private val topBarMinTopPadding = 16.dp
private val actionButtonSize = 48.dp
private val actionIconSize = 20.dp

@Composable
internal fun BooksTopBar(
    state: BooksUiState,
    isScrolled: Boolean,
    onEvent: (BooksUiEvent) -> Unit,
) {
    val successState = state as? BooksUiState.Success
    val statusBarTop = WindowInsets.safeDrawing
        .only(WindowInsetsSides.Top)
        .asPaddingValues()
        .calculateTopPadding()
    Surface(
        shadowElevation = if (isScrolled) 4.dp else 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = topBarMaxWidth)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(
                        top = statusBarTop.coerceAtLeast(topBarMinTopPadding),
                        bottom = 12.dp,
                    ).padding(horizontal = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BooksSearchField(
                        query = successState?.searchQuery.orEmpty(),
                        onEvent = onEvent,
                        modifier = Modifier.weight(1f),
                    )
                    Box {
                        BooksActionButton(
                            icon = Icons.Default.SortByAlpha,
                            contentDescription = stringResource(R.string.books_sort),
                            isSelected = successState?.sortOrder != null,
                            onClick = { onEvent(BooksUiEvent.OnToggleSortMenu) },
                        )
                        if (successState != null) {
                            BooksDropdownMenu(
                                isExpanded = successState.isSortMenuVisible,
                                onDismissRequest = { onEvent(BooksUiEvent.OnDismissSortMenu) },
                                options = BookSortOrder.entries.map { order ->
                                    BooksMenuOption(
                                        title = stringResource(order.toLabelRes()),
                                        isSelected = successState.sortOrder == order,
                                        onClick = { onEvent(BooksUiEvent.OnSortOrderSelect(order)) },
                                    )
                                },
                            )
                        }
                    }
                    Box {
                        BooksActionButton(
                            icon = Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.books_filter),
                            isSelected = successState?.filterOptions?.any { it.isSelected } == true,
                            onClick = { onEvent(BooksUiEvent.OnToggleFilterMenu) },
                        )
                        if (successState != null) {
                            BooksDropdownMenu(
                                isExpanded = successState.isFilterMenuVisible,
                                onDismissRequest = { onEvent(BooksUiEvent.OnDismissFilterMenu) },
                                options = successState.filterOptions.map { option ->
                                    BooksMenuOption(
                                        title = stringResource(option.type.toLabelRes()),
                                        isSelected = option.isSelected,
                                        onClick = { onEvent(BooksUiEvent.OnToggleFilter(option.type)) },
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BooksSearchField(
    query: String,
    onEvent: (BooksUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = { onEvent(BooksUiEvent.OnSearchQueryChange(it)) },
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                placeholder = {
                    Text(
                        text = stringResource(R.string.books_search),
                        modifier = Modifier
                            .alpha(0.6f)
                            .fillMaxWidth(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.books_search_icon),
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onEvent(BooksUiEvent.OnSearchQueryChange("")) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.books_clear_search),
                            )
                        }
                    }
                },
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier = modifier,
        shape = CircleShape,
    ) {}
}

@Composable
private fun BooksActionButton(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(actionButtonSize)
            .semantics { selected = isSelected },
        shape = CircleShape,
        color = if (isSelected) {
            colorScheme.primary.copy(alpha = 0.3f)
        } else {
            colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) colorScheme.primary else colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
                modifier = Modifier.size(actionIconSize),
            )
        }
    }
}

private fun BookSortOrder.toLabelRes(): Int = when (this) {
    BookSortOrder.AlphabeticalAscending -> R.string.books_sort_alphabetical_ascending
    BookSortOrder.AlphabeticalDescending -> R.string.books_sort_alphabetical_descending
}

private fun BookFilterType.toLabelRes(): Int = when (this) {
    BookFilterType.OnlyRead -> R.string.books_filter_read
    BookFilterType.OnlyUnread -> R.string.books_filter_unread
    BookFilterType.Favorites -> R.string.books_filter_favorites
}
