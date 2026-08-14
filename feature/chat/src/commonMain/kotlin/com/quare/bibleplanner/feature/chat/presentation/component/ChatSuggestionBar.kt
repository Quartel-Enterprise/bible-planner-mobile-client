package com.quare.bibleplanner.feature.chat.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bibleplanner.feature.chat.generated.resources.Res
import bibleplanner.feature.chat.generated.resources.chat_suggestion_bar_many
import bibleplanner.feature.chat.generated.resources.chat_suggestion_bar_single
import com.quare.bibleplanner.ui.component.icon.ArrowRotationIcon
import org.jetbrains.compose.resources.stringResource

private val barShape = RoundedCornerShape(14.dp)
private val bulbSize = 18.dp
private val listMaxHeight = 186.dp
private val itemSpacing = 6.dp
private val scrollbarWidth = 4.dp
private val scrollbarGutter = 8.dp
private val scrollbarMinThumb = 24.dp
private const val EXPAND_MILLIS = 280
private const val FADE_MILLIS = 200

@Composable
internal fun ChatSuggestionBar(
    suggestions: List<String>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = barShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(bulbSize),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = if (suggestions.size == 1) {
                        stringResource(Res.string.chat_suggestion_bar_single)
                    } else {
                        stringResource(Res.string.chat_suggestion_bar_many, suggestions.size)
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                )
                ArrowRotationIcon(isUp = isExpanded)
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(EXPAND_MILLIS)) +
                    fadeIn(animationSpec = tween(FADE_MILLIS)),
                exit = shrinkVertically(animationSpec = tween(EXPAND_MILLIS)) +
                    fadeOut(animationSpec = tween(FADE_MILLIS)),
            ) {
                val listState = rememberLazyListState()
                val thumbColor = MaterialTheme.colorScheme.outlineVariant
                // Capped and scrollable: the questions a study hands over run long, and the list
                // must not grow until it is the screen.
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = listMaxHeight)
                        .padding(top = 8.dp)
                        .verticalScrollbar(
                            listState = listState,
                            color = thumbColor,
                        ),
                    verticalArrangement = Arrangement.spacedBy(itemSpacing),
                    contentPadding = PaddingValues(end = scrollbarGutter),
                ) {
                    items(
                        items = suggestions,
                        key = { suggestion -> suggestion },
                    ) { suggestion ->
                        ChatSuggestionRow(
                            suggestion = suggestion,
                            onClick = { onSuggestionClick(suggestion) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * The list's own scrollbar, since there are more suggestions than fit and nothing else says so.
 * Drawn from an average item height: the rows differ by a line at most, and the thumb only has to
 * report roughly where the reader is.
 */
private fun Modifier.verticalScrollbar(
    listState: LazyListState,
    color: Color,
): Modifier = drawWithContent {
    drawContent()
    val layoutInfo = listState.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return@drawWithContent
    val spacingPx = itemSpacing.toPx()
    val averageItem = visibleItems.sumOf { item -> item.size } / visibleItems.size.toFloat() + spacingPx
    val contentHeight = averageItem * layoutInfo.totalItemsCount - spacingPx
    val viewportHeight = size.height
    if (contentHeight <= viewportHeight) return@drawWithContent
    val scrolled = listState.firstVisibleItemIndex * averageItem + listState.firstVisibleItemScrollOffset
    val thumbHeight = (viewportHeight / contentHeight * viewportHeight).coerceAtLeast(scrollbarMinThumb.toPx())
    val travel = (scrolled / (contentHeight - viewportHeight)).coerceIn(0f, 1f)
    val thumbWidth = scrollbarWidth.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(
            x = size.width - thumbWidth,
            y = travel * (viewportHeight - thumbHeight),
        ),
        size = Size(
            width = thumbWidth,
            height = thumbHeight,
        ),
        cornerRadius = CornerRadius(thumbWidth),
    )
}
