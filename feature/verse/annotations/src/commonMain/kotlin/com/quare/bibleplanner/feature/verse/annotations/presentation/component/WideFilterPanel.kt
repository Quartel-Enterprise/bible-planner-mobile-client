package com.quare.bibleplanner.feature.verse.annotations.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.verse.annotations.generated.resources.Res
import bibleplanner.feature.verse.annotations.generated.resources.book_section
import bibleplanner.feature.verse.annotations.generated.resources.clear_all_filters
import bibleplanner.feature.verse.annotations.generated.resources.highlight_color_section
import bibleplanner.feature.verse.annotations.generated.resources.period_section
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsContentUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import com.quare.bibleplanner.feature.verse.annotations.presentation.utils.bookFilterLabel
import com.quare.bibleplanner.feature.verse.annotations.presentation.utils.icon
import com.quare.bibleplanner.feature.verse.annotations.presentation.utils.labelResource
import com.quare.bibleplanner.feature.verse.annotations.presentation.utils.periodLabel
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WideFilterPanel(
    content: AnnotationsContentUiModel,
    onEvent: (AnnotationsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(
            items = content.typeFilters,
            key = { "type-${it.type.name}" },
        ) { filter ->
            FilterPanelRow(
                label = stringResource(filter.type.labelResource),
                count = filter.count,
                isSelected = filter.isSelected,
                icon = filter.type.icon,
                onClick = { onEvent(AnnotationsUiEvent.OnTypeFilterClick(filter.type)) },
            )
        }
        item(key = "colors") {
            VerticalSpacer(18)
            FilterSectionHeader(title = stringResource(Res.string.highlight_color_section))
            val isAnyColorSelected = content.colorFilters.any { it.isSelected }
            FlowRow(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                content.colorFilters.forEach { filter ->
                    ColorFilterToggle(
                        filter = filter,
                        isAnyColorSelected = isAnyColorSelected,
                        onClick = { onEvent(AnnotationsUiEvent.OnColorFilterClick(filter.color)) },
                    )
                }
            }
            VerticalSpacer(18)
            FilterSectionHeader(title = stringResource(Res.string.period_section))
            VerticalSpacer(6)
        }
        items(
            items = content.periodFilters,
            key = { "period-${it.period.name}" },
        ) { filter ->
            FilterPanelRow(
                label = periodLabel(
                    period = filter.period,
                    customRange = content.customRange,
                    today = content.today,
                    isChip = false,
                ),
                count = filter.count,
                isSelected = filter.isSelected,
                onClick = { onEvent(AnnotationsUiEvent.OnPeriodSelected(filter.period)) },
            )
        }
        item(key = "book_header") {
            VerticalSpacer(18)
            FilterSectionHeader(title = stringResource(Res.string.book_section))
            VerticalSpacer(6)
        }
        items(
            items = content.bookFilters,
            key = { "book-${it.bookId?.name}" },
        ) { filter ->
            FilterPanelRow(
                label = bookFilterLabel(filter.bookId),
                count = filter.count,
                isSelected = filter.isSelected,
                onClick = { onEvent(AnnotationsUiEvent.OnBookSelected(filter.bookId)) },
            )
        }
        if (content.hasActiveFilters) {
            item(key = "clear") {
                TextButton(onClick = { onEvent(AnnotationsUiEvent.OnClearFiltersClick) }) {
                    Text(text = stringResource(Res.string.clear_all_filters))
                }
            }
        }
    }
}
