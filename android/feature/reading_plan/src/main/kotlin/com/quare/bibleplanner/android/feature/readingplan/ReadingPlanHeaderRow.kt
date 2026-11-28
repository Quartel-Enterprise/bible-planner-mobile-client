package com.quare.bibleplanner.android.feature.readingplan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.feature.readingplan.presentation.model.OverflowOption
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent

private val segmentedButtonsBreathingRoom = 32.dp
private val menuOffset = DpOffset(
    x = 0.dp,
    y = 8.dp,
)

@Composable
internal fun ReadingPlanHeaderRow(
    selectedReadingPlan: ReadingPlanType,
    isShowingMenu: Boolean,
    isShowingOrderMenu: Boolean,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AdaptiveOrderPicker(
            modifier = Modifier.weight(1f),
            selectedReadingPlan = selectedReadingPlan,
            isShowingOrderMenu = isShowingOrderMenu,
            onEvent = onEvent,
        )
        OverflowMenuButton(
            isShowingMenu = isShowingMenu,
            onEvent = onEvent,
        )
    }
}

/*
 * Why: the segmented buttons show whenever both labels fit in one line, whichever one is selected; otherwise
 * a dropdown chip takes their place, as in the shared screen.
 */
@Composable
private fun AdaptiveOrderPicker(
    selectedReadingPlan: ReadingPlanType,
    isShowingOrderMenu: Boolean,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val onPlanClick: (ReadingPlanType) -> Unit = { onEvent(ReadingPlanUiEvent.OnPlanClick(it)) }
    SubcomposeLayout(modifier = modifier) { constraints ->
        val labelConstraints = constraints.copy(minWidth = 0)
        val segmentedFits = if (constraints.hasBoundedWidth) {
            val probeMaxWidth = constraints.maxWidth - segmentedButtonsBreathingRoom.roundToPx()
            val probeConstraints = labelConstraints.copy(maxWidth = probeMaxWidth.coerceAtLeast(0))
            var overflowed = false
            ReadingPlanType.entries.forEach { probedPlan ->
                subcompose(OrderSelectorSlot.Probe to probedPlan) {
                    PlanTypesSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth(),
                        selectedReadingPlan = probedPlan,
                        onPlanClick = onPlanClick,
                        onLabelOverflow = { overflowed = true },
                    )
                }.forEach { it.measure(probeConstraints) }
            }
            !overflowed
        } else {
            true
        }
        val placeables = subcompose(OrderSelectorSlot.Content) {
            if (segmentedFits) {
                PlanTypesSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth(),
                    selectedReadingPlan = selectedReadingPlan,
                    onPlanClick = onPlanClick,
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    OrderDropdownChip(
                        selectedReadingPlan = selectedReadingPlan,
                        isShowingOrderMenu = isShowingOrderMenu,
                        onEvent = onEvent,
                    )
                }
            }
        }.map { it.measure(labelConstraints) }
        val width = if (constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else {
            placeables.maxOfOrNull { it.width } ?: 0
        }
        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(width, height) {
            placeables.forEach { placeable ->
                placeable.place(
                    x = 0,
                    y = 0,
                )
            }
        }
    }
}

@Composable
private fun PlanTypesSegmentedButtonRow(
    selectedReadingPlan: ReadingPlanType,
    onPlanClick: (ReadingPlanType) -> Unit,
    modifier: Modifier = Modifier,
    onLabelOverflow: () -> Unit = {},
) {
    val types = ReadingPlanType.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        types.forEachIndexed { index, type ->
            SegmentedButton(
                selected = type == selectedReadingPlan,
                onClick = { onPlanClick(type) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = types.size,
                ),
            ) {
                Text(
                    text = stringResource(type.toLabelRes()),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { textLayoutResult ->
                        if (textLayoutResult.hasVisualOverflow) {
                            onLabelOverflow()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun OrderDropdownChip(
    selectedReadingPlan: ReadingPlanType,
    isShowingOrderMenu: Boolean,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    Box {
        Surface(
            modifier = Modifier.semantics { role = Role.DropdownList },
            onClick = { onEvent(ReadingPlanUiEvent.OnOrderMenuClick) },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
            ),
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 8.dp,
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    modifier = Modifier.weight(
                        weight = 1f,
                        fill = false,
                    ),
                    text = stringResource(selectedReadingPlan.toLabelRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = if (isShowingOrderMenu) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        DropdownMenu(
            expanded = isShowingOrderMenu,
            onDismissRequest = { onEvent(ReadingPlanUiEvent.OnOrderMenuDismiss) },
            offset = menuOffset,
        ) {
            ReadingPlanType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(type.toLabelRes())) },
                    onClick = { onEvent(ReadingPlanUiEvent.OnPlanClick(type)) },
                    trailingIcon = if (type == selectedReadingPlan) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.reading_plan_selected),
                                tint = MaterialTheme.colorScheme.primary,
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
private fun OverflowMenuButton(
    isShowingMenu: Boolean,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    Box {
        IconButton(onClick = { onEvent(ReadingPlanUiEvent.OnOverflowClick) }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.reading_plan_more_options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(
            expanded = isShowingMenu,
            onDismissRequest = { onEvent(ReadingPlanUiEvent.OnOverflowDismiss) },
            offset = menuOffset,
        ) {
            OverflowOption.entries.forEach { option ->
                OverflowMenuItem(
                    option = option,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun OverflowMenuItem(
    option: OverflowOption,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val contentColor = if (option == OverflowOption.DELETE_PROGRESS) {
        MaterialTheme.colorScheme.error
    } else {
        LocalContentColor.current
    }
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(option.toTitleRes()),
                color = contentColor,
            )
        },
        onClick = { onEvent(ReadingPlanUiEvent.OnOverflowOptionClick(option)) },
        leadingIcon = {
            Icon(
                imageVector = option.toIcon(),
                contentDescription = null,
                tint = contentColor,
            )
        },
    )
}

private enum class OrderSelectorSlot {
    Probe,
    Content,
}

private fun ReadingPlanType.toLabelRes(): Int = when (this) {
    ReadingPlanType.CHRONOLOGICAL -> R.string.reading_plan_chronological_order
    ReadingPlanType.BOOKS -> R.string.reading_plan_book_order
}

private fun OverflowOption.toTitleRes(): Int = when (this) {
    OverflowOption.EDIT_START_DAY -> R.string.reading_plan_edit_plan_start_day
    OverflowOption.DELETE_PROGRESS -> R.string.reading_plan_delete_progress_option
}

private fun OverflowOption.toIcon(): ImageVector = when (this) {
    OverflowOption.EDIT_START_DAY -> Icons.Default.Edit
    OverflowOption.DELETE_PROGRESS -> Icons.Default.Delete
}
