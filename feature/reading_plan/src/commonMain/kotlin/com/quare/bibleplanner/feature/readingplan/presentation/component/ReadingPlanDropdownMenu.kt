package com.quare.bibleplanner.feature.readingplan.presentation.component

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.readingplan.presentation.factory.ReadingPlanMenuOptionsFactory
import com.quare.bibleplanner.feature.readingplan.presentation.model.OverflowOption
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReadingPlanDropdownMenu(
    isShowingMenu: Boolean,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    DropdownMenu(
        expanded = isShowingMenu,
        onDismissRequest = {
            onEvent(ReadingPlanUiEvent.OnOverflowDismiss)
        },
    ) {
        ReadingPlanMenuOptionsFactory.options.forEach { option ->
            val tint = if (option.type == OverflowOption.DELETE_PROGRESS) {
                MaterialTheme.colorScheme.error
            } else {
                LocalContentColor.current
            }
            val text = stringResource(option.name)
            DropdownMenuItem(
                text = {
                    Text(
                        text = text,
                        color = tint
                    )
                },
                onClick = {
                    onEvent(ReadingPlanUiEvent.OnOverflowOptionClick(option.type))
                },
                leadingIcon = {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = text,
                        tint = tint,
                    )
                },
            )
        }
    }
}
