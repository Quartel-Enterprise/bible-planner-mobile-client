package com.quare.bibleplanner.feature.books.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.content_description_clear_search
import bibleplanner.feature.books.generated.resources.content_description_dismiss
import bibleplanner.feature.books.generated.resources.content_description_info
import bibleplanner.feature.books.generated.resources.content_description_search
import bibleplanner.feature.books.generated.resources.content_description_selected
import bibleplanner.feature.books.generated.resources.grid
import bibleplanner.feature.books.generated.resources.list
import bibleplanner.feature.books.generated.resources.reading_not_available_yet
import bibleplanner.feature.books.generated.resources.search_books
import bibleplanner.feature.books.generated.resources.sort_alphabetical_ascending
import bibleplanner.feature.books.generated.resources.sort_alphabetical_descending
import com.quare.bibleplanner.feature.books.presentation.binding.BookGroup
import com.quare.bibleplanner.feature.books.presentation.binding.BookTestament
import com.quare.bibleplanner.feature.books.presentation.component.BookItemComponent
import com.quare.bibleplanner.feature.books.presentation.model.BookFilterOption
import com.quare.bibleplanner.feature.books.presentation.model.BookFilterType
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BookSortOrder
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.utils.LocalMainPadding
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BooksScreen(
    state: BooksUiState,
    onEvent: (BooksUiEvent) -> Unit,
) {
    val mainPadding = LocalMainPadding.current
    val lazyGridState = rememberLazyGridState()

    val isScrolled by remember {
        derivedStateOf {
            lazyGridState.firstVisibleItemIndex > 0 || lazyGridState.firstVisibleItemScrollOffset > 0
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
                        lazyGridState = lazyGridState,
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
                .background(MaterialTheme.colorScheme.background)
                .padding(top = contentPadding.calculateTopPadding(), bottom = 12.dp)
                .padding(horizontal = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val successState = state as? BooksUiState.Success
                BooksSearchBar(
                    query = successState?.searchQuery.orEmpty(),
                    onQueryChange = { onEvent(BooksUiEvent.OnSearchQueryChange(it)) },
                    onClearSearch = { onEvent(BooksUiEvent.OnSearchQueryChange("")) },
                    modifier = Modifier.weight(1f),
                    shape = CircleShape,
                )

                ActionCircleButton(
                    imageVector = Icons.Default.SortByAlpha,
                    onClick = {
                        if (successState != null) {
                            onEvent(BooksUiEvent.OnToggleSortMenu(!successState.isSortMenuVisible))
                        }
                    },
                )

                ActionCircleButton(
                    imageVector = Icons.Default.FilterList,
                    onClick = {
                        if (successState != null) {
                            onEvent(BooksUiEvent.OnToggleFilterMenu(!successState.isFilterMenuVisible))
                        }
                    },
                )
            }
            if (state is BooksUiState.Success) {
                // Dropdown menus for sort and filter
                Box(modifier = Modifier.fillMaxWidth()) {
                    BooksSortMenu(
                        isVisible = state.isSortMenuVisible,
                        currentOrder = state.sortOrder,
                        onToggleMenu = { onEvent(BooksUiEvent.OnToggleSortMenu(it)) },
                        onDismiss = { onEvent(BooksUiEvent.OnDismissSortMenu) },
                        onOrderSelect = { onEvent(BooksUiEvent.OnSortOrderSelect(it)) },
                    )
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
private fun ActionCircleButton(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun BooksInformationBox(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(Res.string.content_description_info),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )

            HorizontalSpacer(16.dp)

            Text(
                text = stringResource(Res.string.reading_not_available_yet),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            HorizontalSpacer(12.dp)

            CommonIconButton(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.content_description_dismiss),
                onClick = onDismiss,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BooksSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = SearchBarDefaults.dockedShape,
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
        shape = shape,
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
    Box(modifier = Modifier.fillMaxWidth()) {
        // Added fillMaxWidth to ensure dropdown anchors correctly
        // The IconButton is now replaced by ActionCircleButton in BooksTopBar,
        // but the DropdownMenu still needs to be here and its visibility controlled.
        // The anchor for the dropdown will implicitly be the Box or its parent.
        DropdownMenu(
            expanded = isVisible,
            onDismissRequest = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd), // Align to the top end
        ) {
            BookSortOrder.values().forEach { order ->
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
    Box(modifier = Modifier.fillMaxWidth()) {
        // Added fillMaxWidth to ensure dropdown anchors correctly
        // The IconButton is now replaced by ActionCircleButton in BooksTopBar,
        // but the DropdownMenu still needs to be here and its visibility controlled.
        // The anchor for the dropdown will implicitly be the Box or its parent.
        DropdownMenu(
            expanded = isVisible,
            onDismissRequest = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd), // Align to the top end
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

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun BookList(
    state: BooksUiState.Success,
    lazyGridState: LazyGridState,
    onEvent: (BooksUiEvent) -> Unit,
) {
    SharedTransitionLayout {
        val sharedTransitionScope = this
        AnimatedContent(
            targetState = state.layoutFormat,
            label = "layout_transition",
            transitionSpec = {
                EnterTransition.None togetherWith ExitTransition.None
            },
        ) { targetLayoutFormat ->
            val animatedVisibilityScope = this
            val isGrid = targetLayoutFormat == BookLayoutFormat.Grid
            val totalColumns = 2

            LazyVerticalGrid(
                state = lazyGridState,
                columns = GridCells.Fixed(totalColumns),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(span = { GridItemSpan(totalColumns) }) {
                    AnimatedVisibility(
                        visible = state.isInformationBoxVisible,
                        enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
                        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(),
                    ) {
                        BooksInformationBox(
                            onDismiss = { onEvent(BooksUiEvent.OnDismissInformationBox) },
                        )
                    }
                }

                if (state.shouldShowTestamentToggle) {
                    item(span = { GridItemSpan(totalColumns) }) {
                        Toggles(
                            selectedTestament = state.selectedTestament,
                            onTestamentSelect = { onEvent(BooksUiEvent.OnTestamentSelect(it)) },
                            selectedLayoutFormat = state.layoutFormat,
                            onLayoutFormatSelect = { onEvent(BooksUiEvent.OnLayoutFormatSelect(it)) },
                        )
                    }
                }

                if (!state.shouldShowTestamentToggle) {
                    items(
                        items = state.filteredBooks,
                        key = { it.id.name },
                        span = { GridItemSpan(if (isGrid) 1 else totalColumns) },
                    ) { book ->
                        BookItemComponent(
                            book = book,
                            layoutFormat = targetLayoutFormat,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            onClick = { onEvent(BooksUiEvent.OnBookClick(book)) },
                            onToggleFavorite = { onEvent(BooksUiEvent.OnToggleFavorite(book.id)) },
                        )
                    }
                } else {
                    state.groupsInTestament.forEach { presentationGroup ->
                        item(span = { GridItemSpan(totalColumns) }) {
                            BookGroupHeader(presentationGroup.group)
                        }

                        items(
                            items = presentationGroup.books,
                            key = { it.id.name },
                            span = { GridItemSpan(if (isGrid) 1 else totalColumns) },
                        ) { book ->
                            BookItemComponent(
                                book = book,
                                layoutFormat = targetLayoutFormat,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                onClick = { onEvent(BooksUiEvent.OnBookClick(book)) },
                                onToggleFavorite = { onEvent(BooksUiEvent.OnToggleFavorite(book.id)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Toggles(
    selectedTestament: BookTestament,
    onTestamentSelect: (BookTestament) -> Unit,
    selectedLayoutFormat: BookLayoutFormat,
    onLayoutFormatSelect: (BookLayoutFormat) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1.2f)) {
            TestamentToggle(
                selectedTestament = selectedTestament,
                onTestamentSelect = onTestamentSelect,
            )
        }
        Box(modifier = Modifier.weight(0.8f)) {
            LayoutToggle(
                selectedLayoutFormat = selectedLayoutFormat,
                onLayoutFormatSelect = onLayoutFormatSelect,
            )
        }
    }
}

@Composable
private fun LayoutToggle(
    selectedLayoutFormat: BookLayoutFormat,
    onLayoutFormatSelect: (BookLayoutFormat) -> Unit,
) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = CircleShape,
            ).padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val formats = listOf(BookLayoutFormat.List, BookLayoutFormat.Grid)
        formats.forEach { format ->
            val isSelected = selectedLayoutFormat == format
            val shape = when (format) {
                BookLayoutFormat.List -> CircleShape
                BookLayoutFormat.Grid -> RoundedCornerShape(10.dp)
            }

            Box(
                modifier = Modifier
                    .height(32.dp)
                    .weight(1f)
                    .clip(shape)
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        },
                    ).clickable { onLayoutFormatSelect(format) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when (format) {
                        BookLayoutFormat.List -> Icons.Default.List
                        BookLayoutFormat.Grid -> Icons.Default.GridView
                    },
                    contentDescription = stringResource(
                        when (format) {
                            BookLayoutFormat.List -> Res.string.list
                            BookLayoutFormat.Grid -> Res.string.grid
                        },
                    ),
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun TestamentToggle(
    selectedTestament: BookTestament,
    onTestamentSelect: (BookTestament) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val testaments = BookTestament.entries
        testaments.forEachIndexed { index, testament ->
            SegmentedButton(
                selected = selectedTestament == testament,
                onClick = { onTestamentSelect(testament) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = testaments.size,
                ),
                icon = {},
                modifier = Modifier.height(36.dp),
            ) {
                Text(
                    text = stringResource(testament.titleRes),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    softWrap = false,
                )
            }
        }
    }
}

@Composable
private fun BookGroupHeader(group: BookGroup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(group.titleRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(end = 8.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        )
    }
}
