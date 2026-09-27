package com.quare.bibleplanner.feature.verse.annotations.presentation.component

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationPeriod
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsContentUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import com.quare.bibleplanner.feature.verse.annotations.presentation.utils.bookFilterLabel
import com.quare.bibleplanner.feature.verse.annotations.presentation.utils.labelResource
import com.quare.bibleplanner.feature.verse.annotations.presentation.utils.periodLabel
import com.quare.bibleplanner.ui.component.AppDropdownMenuItem
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun bookFilterMenuItems(
    content: AnnotationsContentUiModel,
    onEvent: (AnnotationsUiEvent) -> Unit,
): List<AppDropdownMenuItem> = content.bookFilters.map { filter ->
    AppDropdownMenuItem(
        title = "${bookFilterLabel(filter.bookId)} (${filter.count})",
        icon = null,
        isSelected = filter.isSelected,
        isDestructive = false,
        onClick = { onEvent(AnnotationsUiEvent.OnBookSelected(filter.bookId)) },
    )
}

@Composable
internal fun periodFilterMenuItems(
    content: AnnotationsContentUiModel,
    onEvent: (AnnotationsUiEvent) -> Unit,
): List<AppDropdownMenuItem> = content.periodFilters.map { filter ->
    AppDropdownMenuItem(
        title = if (filter.period == AnnotationPeriod.CUSTOM && content.customRange == null) {
            stringResource(filter.period.labelResource)
        } else {
            val label = periodLabel(
                period = filter.period,
                customRange = content.customRange,
                today = content.today,
                isChip = false,
            )
            "$label (${filter.count})"
        },
        icon = null,
        isSelected = filter.isSelected,
        isDestructive = false,
        onClick = { onEvent(AnnotationsUiEvent.OnPeriodSelected(filter.period)) },
    )
}
