package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import com.quare.bibleplanner.ui.component.ActionCircleButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BooksTopBar(
    modifier: Modifier = Modifier,
    state: BooksUiState,
    onEvent: (BooksUiEvent) -> Unit,
    contentPadding: PaddingValues,
    isScrolled: Boolean,
) {
    Surface(
        shadowElevation = if (isScrolled) 4.dp else 0.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = contentPadding.calculateTopPadding().coerceAtLeast(16.dp), bottom = 12.dp)
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
                        onEvent = onEvent,
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                    )

                    Box {
                        ActionCircleButton(
                            imageVector = Icons.Default.SortByAlpha,
                            onClick = { onEvent(BooksUiEvent.OnToggleSortMenu) },
                            isSelected = successState?.sortOrder != null,
                        )
                        if (successState != null) {
                            BooksSortMenu(
                                isVisible = successState.isSortMenuVisible,
                                currentOrder = successState.sortOrder,
                                onEvent = onEvent,
                            )
                        }
                    }

                    Box {
                        ActionCircleButton(
                            imageVector = Icons.Default.FilterList,
                            onClick = { onEvent(BooksUiEvent.OnToggleFilterMenu) },
                            isSelected = successState?.filterOptions?.any { it.isSelected } == true,
                        )
                        if (successState != null) {
                            BooksFilterMenu(
                                isVisible = successState.isFilterMenuVisible,
                                filterOptions = successState.filterOptions,
                                onEvent = onEvent,
                            )
                        }
                    }
                }
            }
        }
    }
}
