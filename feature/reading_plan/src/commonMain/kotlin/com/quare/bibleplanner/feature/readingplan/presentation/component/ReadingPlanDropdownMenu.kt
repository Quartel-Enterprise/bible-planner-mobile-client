package com.quare.bibleplanner.feature.readingplan.presentation.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.readingplan.presentation.factory.ReadingPlanMenuOptionsFactory
import com.quare.bibleplanner.feature.readingplan.presentation.model.OverflowOption
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.ui.component.AppDropdownMenu
import com.quare.bibleplanner.ui.component.AppDropdownMenuItem
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BoxScope.ReadingPlanDropdownMenu(
    isShowingMenu: Boolean,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    AppDropdownMenu(
        isExpanded = isShowingMenu,
        onDismissRequest = { onEvent(ReadingPlanUiEvent.OnOverflowDismiss) },
        items = ReadingPlanMenuOptionsFactory.options.map { option ->
            AppDropdownMenuItem(
                title = stringResource(option.name),
                icon = option.icon,
                isSelected = false,
                isDestructive = option.type == OverflowOption.DELETE_PROGRESS,
                onClick = { onEvent(ReadingPlanUiEvent.OnOverflowOptionClick(option.type)) },
            )
        },
    )
}
