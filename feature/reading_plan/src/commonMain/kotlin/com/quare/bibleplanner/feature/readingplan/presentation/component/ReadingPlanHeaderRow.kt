package com.quare.bibleplanner.feature.readingplan.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReadingPlanHeaderRow(
    selectedReadingPlan: ReadingPlanType,
    isShowingMenu: Boolean,
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
            onPlanClick = { onEvent(ReadingPlanUiEvent.OnPlanClick(it)) },
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
    onPlanClick: (ReadingPlanType) -> Unit,
    modifier: Modifier = Modifier,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val labelConstraints = constraints.copy(minWidth = 0)
        val segmentedFits = if (constraints.hasBoundedWidth) {
            var overflowed = false
            subcompose(OrderSelectorSlot.Probe) {
                PlanTypesSegmentedButtons(
                    modifier = Modifier.fillMaxWidth(),
                    selectedReadingPlan = selectedReadingPlan,
                    onPlanClick = onPlanClick,
                    onLabelOverflow = { overflowed = true },
                )
            }.forEach { it.measure(labelConstraints) }
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
                OrderChip(
                    selectedReadingPlan = selectedReadingPlan,
                    onClick = { onPlanClick(selectedReadingPlan.toggled()) },
                )
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
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
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(selectedReadingPlan.labelResource()),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
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
                imageVector = Icons.Default.MoreVert,
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

private fun ReadingPlanType.toggled(): ReadingPlanType = when (this) {
    ReadingPlanType.CHRONOLOGICAL -> ReadingPlanType.BOOKS
    ReadingPlanType.BOOKS -> ReadingPlanType.CHRONOLOGICAL
}

private fun ReadingPlanType.labelResource(): StringResource = when (this) {
    ReadingPlanType.CHRONOLOGICAL -> Res.string.chronological_order
    ReadingPlanType.BOOKS -> Res.string.book_order
}
