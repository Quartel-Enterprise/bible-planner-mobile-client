package com.quare.bibleplanner.android.feature.annotations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsContentUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent

private const val SELECTED_BACKGROUND_ALPHA = 0.12f
private val sectionSpacing = 18.dp
private val headerSpacing = 6.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AnnotationsFilterPanel(
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
            AnnotationsFilterPanelRow(
                label = stringResource(filter.type.toLabelRes()),
                count = filter.count,
                isSelected = filter.isSelected,
                icon = filter.type.toIcon(),
                onClick = { onEvent(AnnotationsUiEvent.OnTypeFilterClick(filter.type)) },
            )
        }
        item(key = "colors") {
            Spacer(modifier = Modifier.height(sectionSpacing))
            AnnotationsFilterSectionHeader(title = stringResource(R.string.annotations_highlight_color_section))
            val isAnyColorSelected = content.colorFilters.any { it.isSelected }
            FlowRow(
                modifier = Modifier.padding(top = headerSpacing),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                content.colorFilters.forEach { filter ->
                    AnnotationsColorFilterToggle(
                        filter = filter,
                        isAnyColorSelected = isAnyColorSelected,
                        onClick = { onEvent(AnnotationsUiEvent.OnColorFilterClick(filter.color)) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(sectionSpacing))
            AnnotationsFilterSectionHeader(title = stringResource(R.string.annotations_period_section))
            Spacer(modifier = Modifier.height(headerSpacing))
        }
        items(
            items = content.periodFilters,
            key = { "period-${it.period.name}" },
        ) { filter ->
            AnnotationsFilterPanelRow(
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
            Spacer(modifier = Modifier.height(sectionSpacing))
            AnnotationsFilterSectionHeader(title = stringResource(R.string.annotations_book_section))
            Spacer(modifier = Modifier.height(headerSpacing))
        }
        items(
            items = content.bookFilters,
            key = { "book-${it.bookId?.name}" },
        ) { filter ->
            AnnotationsFilterPanelRow(
                label = bookFilterLabel(filter.bookId),
                count = filter.count,
                isSelected = filter.isSelected,
                onClick = { onEvent(AnnotationsUiEvent.OnBookSelected(filter.bookId)) },
            )
        }
        if (content.hasActiveFilters) {
            item(key = "clear") {
                TextButton(onClick = { onEvent(AnnotationsUiEvent.OnClearFiltersClick) }) {
                    Text(text = stringResource(R.string.annotations_clear_all_filters))
                }
            }
        }
    }
}

@Composable
private fun AnnotationsFilterSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun AnnotationsFilterPanelRow(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = SELECTED_BACKGROUND_ALPHA)
                } else {
                    Color.Transparent
                },
            ).selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick,
            ).padding(
                horizontal = 12.dp,
                vertical = 9.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        icon?.let { safeIcon ->
            Icon(
                imageVector = safeIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}
