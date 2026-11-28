package com.quare.bibleplanner.android.feature.annotations

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationFilterMenu
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationPeriod
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsContentUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent

@Composable
internal fun AnnotationsFilterBar(
    content: AnnotationsContentUiModel,
    openFilterMenu: AnnotationFilterMenu?,
    onEvent: (AnnotationsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = content.typeFilters,
                key = { it.type.name },
            ) { filter ->
                FilterChip(
                    selected = filter.isSelected,
                    onClick = { onEvent(AnnotationsUiEvent.OnTypeFilterClick(filter.type)) },
                    label = { Text(text = stringResource(filter.type.toLabelRes())) },
                    leadingIcon = {
                        Icon(
                            imageVector = filter.type.toIcon(),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
        }
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AnnotationsDropdownFilterChip(
                label = content.selectedBookId?.getBookName() ?: stringResource(R.string.annotations_filter_book),
                icon = Icons.AutoMirrored.Filled.MenuBook,
                isActive = content.selectedBookId != null,
                isExpanded = openFilterMenu == AnnotationFilterMenu.BOOK,
                onClick = { onEvent(AnnotationsUiEvent.OnBookFilterClick) },
                onDismissRequest = { onEvent(AnnotationsUiEvent.OnFilterMenuDismiss) },
            ) {
                content.bookFilters.forEach { filter ->
                    AnnotationsFilterMenuItem(
                        title = "${bookFilterLabel(filter.bookId)} (${filter.count})",
                        isSelected = filter.isSelected,
                        onClick = { onEvent(AnnotationsUiEvent.OnBookSelected(filter.bookId)) },
                    )
                }
            }
            AnnotationsDropdownFilterChip(
                label = periodLabel(
                    period = content.selectedPeriod,
                    customRange = content.customRange,
                    today = content.today,
                    isChip = true,
                ),
                icon = Icons.Default.CalendarMonth,
                isActive = content.selectedPeriod != AnnotationPeriod.ANY,
                isExpanded = openFilterMenu == AnnotationFilterMenu.PERIOD,
                onClick = { onEvent(AnnotationsUiEvent.OnPeriodFilterClick) },
                onDismissRequest = { onEvent(AnnotationsUiEvent.OnFilterMenuDismiss) },
            ) {
                content.periodFilters.forEach { filter ->
                    AnnotationsFilterMenuItem(
                        title = if (filter.period == AnnotationPeriod.CUSTOM && content.customRange == null) {
                            stringResource(filter.period.toLabelRes())
                        } else {
                            val label = periodLabel(
                                period = filter.period,
                                customRange = content.customRange,
                                today = content.today,
                                isChip = false,
                            )
                            "$label (${filter.count})"
                        },
                        isSelected = filter.isSelected,
                        onClick = { onEvent(AnnotationsUiEvent.OnPeriodSelected(filter.period)) },
                    )
                }
            }
            VerticalDivider(
                modifier = Modifier
                    .height(22.dp)
                    .padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            val isAnyColorSelected = content.colorFilters.any { it.isSelected }
            content.colorFilters.forEach { filter ->
                AnnotationsColorFilterToggle(
                    filter = filter,
                    isAnyColorSelected = isAnyColorSelected,
                    onClick = { onEvent(AnnotationsUiEvent.OnColorFilterClick(filter.color)) },
                )
            }
            if (content.hasActiveFilters) {
                TextButton(onClick = { onEvent(AnnotationsUiEvent.OnClearFiltersClick) }) {
                    Text(text = stringResource(R.string.annotations_clear_filters))
                }
            }
        }
    }
}

@Composable
private fun AnnotationsDropdownFilterChip(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onDismissRequest: () -> Unit,
    menuItems: @Composable () -> Unit,
) {
    Box {
        FilterChip(
            selected = isActive,
            onClick = onClick,
            label = { Text(text = label) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
        )
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = onDismissRequest,
        ) {
            menuItems()
        }
    }
}

@Composable
private fun AnnotationsFilterMenuItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(text = title) },
        onClick = onClick,
        trailingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.annotations_selected),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            null
        },
    )
}
