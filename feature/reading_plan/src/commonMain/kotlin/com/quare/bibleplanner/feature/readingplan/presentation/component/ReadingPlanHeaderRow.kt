package com.quare.bibleplanner.feature.readingplan.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.book_order
import bibleplanner.feature.reading_plan.generated.resources.chronological_order
import bibleplanner.feature.reading_plan.generated.resources.more_options
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.ui.component.AppDropdownMenu
import com.quare.bibleplanner.ui.component.AppDropdownMenuItem
import com.quare.bibleplanner.ui.icons.AppIcon
import com.quare.bibleplanner.ui.icons.Icon
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val segmentedButtonsBreathingRoom = 32.dp

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
        AdaptiveOrderSelector(
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

@Composable
private fun AdaptiveOrderSelector(
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
                    PlanTypesSegmentedButtons(
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
                PlanTypesSegmentedButtons(
                    modifier = Modifier.fillMaxWidth(),
                    selectedReadingPlan = selectedReadingPlan,
                    onPlanClick = onPlanClick,
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    OrderChip(
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
            placeables.forEach { placeable -> placeable.place(x = 0, y = 0) }
        }
    }
}

@Composable
private fun OrderChip(
    selectedReadingPlan: ReadingPlanType,
    isShowingOrderMenu: Boolean,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Surface(
            onClick = { onEvent(ReadingPlanUiEvent.OnOrderMenuClick) },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(selectedReadingPlan.toLabelResource()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Icon(
                    modifier = Modifier.size(18.dp),
                    icon = if (isShowingOrderMenu) {
                        AppIcon.KeyboardArrowUp
                    } else {
                        AppIcon.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        OrderDropdownMenu(
            selectedReadingPlan = selectedReadingPlan,
            isShowingOrderMenu = isShowingOrderMenu,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun BoxScope.OrderDropdownMenu(
    selectedReadingPlan: ReadingPlanType,
    isShowingOrderMenu: Boolean,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    AppDropdownMenu(
        isExpanded = isShowingOrderMenu,
        onDismissRequest = { onEvent(ReadingPlanUiEvent.OnOrderMenuDismiss) },
        items = ReadingPlanType.entries.map { type ->
            AppDropdownMenuItem(
                title = stringResource(type.toLabelResource()),
                icon = null,
                isSelected = type == selectedReadingPlan,
                isDestructive = false,
                onClick = { onEvent(ReadingPlanUiEvent.OnPlanClick(type)) },
            )
        },
    )
}

@Composable
private fun OverflowMenuButton(
    isShowingMenu: Boolean,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        IconButton(onClick = { onEvent(ReadingPlanUiEvent.OnOverflowClick) }) {
            Icon(
                icon = AppIcon.MoreVert,
                contentDescription = stringResource(Res.string.more_options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ReadingPlanDropdownMenu(
            isShowingMenu = isShowingMenu,
            onEvent = onEvent,
        )
    }
}

private enum class OrderSelectorSlot {
    Probe,
    Content,
}

private fun ReadingPlanType.toLabelResource(): StringResource = when (this) {
    ReadingPlanType.CHRONOLOGICAL -> Res.string.chronological_order
    ReadingPlanType.BOOKS -> Res.string.book_order
}
