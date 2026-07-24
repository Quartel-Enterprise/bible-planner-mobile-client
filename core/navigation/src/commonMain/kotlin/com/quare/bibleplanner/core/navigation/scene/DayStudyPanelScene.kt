package com.quare.bibleplanner.core.navigation.scene

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPanelRatio

private val handleWidth = 20.dp
private val gripWidth = 4.dp
private val gripHeight = 32.dp

internal class DayStudyPanelScene(
    override val key: Any,
    private val mainEntry: NavEntry<NavKey>,
    private val detailEntry: NavEntry<NavKey>,
    override val previousEntries: List<NavEntry<NavKey>>,
    private val initialReadingFraction: Float,
    private val onReadingFractionCommit: (Float) -> Unit,
) : Scene<NavKey> {
    override val entries: List<NavEntry<NavKey>> = listOf(mainEntry, detailEntry)

    override val content: @Composable () -> Unit = {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            var readingFraction by remember(initialReadingFraction) {
                mutableFloatStateOf(initialReadingFraction)
            }
            val totalWidthPx = constraints.maxWidth.toFloat()
            val layoutDirection = LocalLayoutDirection.current
            val dragSign = if (layoutDirection == LayoutDirection.Ltr) 1f else -1f
            val draggableState = rememberDraggableState { deltaPx ->
                if (totalWidthPx > 0f) {
                    readingFraction = (readingFraction + dragSign * deltaPx / totalWidthPx)
                        .coerceIn(
                            minimumValue = DayStudyPanelRatio.MIN,
                            maximumValue = DayStudyPanelRatio.MAX,
                        )
                }
            }
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(readingFraction)
                        .fillMaxHeight(),
                ) {
                    mainEntry.Content()
                }
                DividerHandle(
                    draggableState = draggableState,
                    onDragStopped = { onReadingFractionCommit(readingFraction) },
                )
                Box(
                    modifier = Modifier
                        .weight(1f - readingFraction)
                        .fillMaxHeight(),
                ) {
                    detailEntry.Content()
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DayStudyPanelScene

        return key == other.key &&
            mainEntry == other.mainEntry &&
            detailEntry == other.detailEntry &&
            previousEntries == other.previousEntries
    }

    override fun hashCode(): Int = key.hashCode() * 31 +
        mainEntry.hashCode() * 31 +
        detailEntry.hashCode() * 31 +
        previousEntries.hashCode() * 31

    override fun toString(): String = "DayStudyPanelScene(key=$key, mainEntry=$mainEntry, detailEntry=$detailEntry, " +
        "previousEntries=$previousEntries)"
}

@Composable
private fun DividerHandle(
    draggableState: DraggableState,
    onDragStopped: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(handleWidth)
            .pointerHoverIcon(PointerIcon.Hand)
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                onDragStopped = { onDragStopped() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        VerticalDivider()
        Box(
            modifier = Modifier
                .width(gripWidth)
                .height(gripHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}
