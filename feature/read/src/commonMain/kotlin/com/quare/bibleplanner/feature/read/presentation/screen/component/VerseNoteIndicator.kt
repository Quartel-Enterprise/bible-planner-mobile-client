package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.open_verse_note
import com.quare.bibleplanner.feature.read.presentation.model.VerseNoteMarkPosition
import org.jetbrains.compose.resources.stringResource

internal val verseNoteIndicatorWidth = 40.dp
private val iconSize = 18.dp
private val barWidth = 2.dp
private val lastBarEndInset = 6.dp
private const val ICON_ALPHA = 0.85f
private const val BAR_ALPHA = 0.45f

// Why: only the verse that carries the icon is announced; the bar on the verses below repeats the
// same action, so it stays tappable but out of the screen reader's way.
@Composable
internal fun VerseNoteIndicator(
    position: VerseNoteMarkPosition,
    firstLineHeight: Dp,
    rowVerticalPadding: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconTop = rowVerticalPadding + (firstLineHeight - iconSize) / 2
    val barColor = MaterialTheme.colorScheme.primary
    val openNoteLabel = stringResource(Res.string.open_verse_note)
    Box(
        modifier = modifier
            .width(verseNoteIndicatorWidth)
            .fillMaxHeight()
            .clickable(
                onClickLabel = openNoteLabel,
                role = Role.Button,
                onClick = onClick,
            ).semantics {
                if (position.hasIcon) {
                    contentDescription = openNoteLabel
                } else {
                    hideFromAccessibility()
                }
            }.drawBehind {
                if (!position.isLinkedAbove && !position.isLinkedBelow) return@drawBehind
                drawVerseNoteBar(
                    color = barColor,
                    top = if (position.isLinkedAbove) 0f else (iconTop + iconSize).toPx(),
                    bottom = if (position.isLinkedBelow) {
                        size.height
                    } else {
                        size.height - (rowVerticalPadding + lastBarEndInset).toPx()
                    },
                )
            },
        contentAlignment = Alignment.TopCenter,
    ) {
        if (position.hasIcon) {
            Icon(
                modifier = Modifier
                    .padding(top = iconTop)
                    .size(iconSize)
                    .alpha(ICON_ALPHA),
                imageVector = Icons.AutoMirrored.Filled.StickyNote2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

internal fun Modifier.drawVerseNoteLink(color: Color): Modifier = drawBehind {
    drawVerseNoteBar(
        color = color,
        top = 0f,
        bottom = size.height,
    )
}

private fun DrawScope.drawVerseNoteBar(
    color: Color,
    top: Float,
    bottom: Float,
) {
    val x = size.width - (verseNoteIndicatorWidth / 2).toPx()
    drawLine(
        color = color.copy(alpha = BAR_ALPHA),
        start = Offset(x = x, y = top),
        end = Offset(x = x, y = bottom),
        strokeWidth = barWidth.toPx(),
    )
}
