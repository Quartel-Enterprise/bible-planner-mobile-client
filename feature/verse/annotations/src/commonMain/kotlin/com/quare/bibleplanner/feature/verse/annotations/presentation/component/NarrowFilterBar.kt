package com.quare.bibleplanner.feature.verse.annotations.presentation.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.verse.annotations.generated.resources.Res
import bibleplanner.feature.verse.annotations.generated.resources.clear_filters
import bibleplanner.feature.verse.annotations.generated.resources.filter_book
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationFilterMenu
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationPeriod
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsContentUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import com.quare.bibleplanner.feature.verse.annotations.presentation.utils.icon
import com.quare.bibleplanner.feature.verse.annotations.presentation.utils.labelResource
import com.quare.bibleplanner.feature.verse.annotations.presentation.utils.periodLabel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NarrowFilterBar(
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
                    label = { Text(text = stringResource(filter.type.labelResource)) },
                    leadingIcon = {
                        Icon(
                            imageVector = filter.type.icon,
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
            DropdownFilterChip(
                label = content.selectedBookId?.getBookName() ?: stringResource(Res.string.filter_book),
                icon = Icons.AutoMirrored.Filled.MenuBook,
                isActive = content.selectedBookId != null,
                isExpanded = openFilterMenu == AnnotationFilterMenu.BOOK,
                menuItems = bookFilterMenuItems(
                    content = content,
                    onEvent = onEvent,
                ),
                onClick = { onEvent(AnnotationsUiEvent.OnBookFilterClick) },
                onDismissRequest = { onEvent(AnnotationsUiEvent.OnFilterMenuDismiss) },
            )
            DropdownFilterChip(
                label = periodLabel(
                    period = content.selectedPeriod,
                    customRange = content.customRange,
                    today = content.today,
                    isChip = true,
                ),
                icon = Icons.Default.CalendarMonth,
                isActive = content.selectedPeriod != AnnotationPeriod.ANY,
                isExpanded = openFilterMenu == AnnotationFilterMenu.PERIOD,
                menuItems = periodFilterMenuItems(
                    content = content,
                    onEvent = onEvent,
                ),
                onClick = { onEvent(AnnotationsUiEvent.OnPeriodFilterClick) },
                onDismissRequest = { onEvent(AnnotationsUiEvent.OnFilterMenuDismiss) },
            )
            VerticalDivider(
                modifier = Modifier
                    .height(22.dp)
                    .padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            val isAnyColorSelected = content.colorFilters.any { it.isSelected }
            content.colorFilters.forEach { filter ->
                ColorFilterToggle(
                    filter = filter,
                    isAnyColorSelected = isAnyColorSelected,
                    onClick = { onEvent(AnnotationsUiEvent.OnColorFilterClick(filter.color)) },
                )
            }
            if (content.hasActiveFilters) {
                TextButton(onClick = { onEvent(AnnotationsUiEvent.OnClearFiltersClick) }) {
                    Text(text = stringResource(Res.string.clear_filters))
                }
            }
        }
    }
}
