package com.quare.bibleplanner.feature.verse.annotations.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.verseannotations.domain.model.AnnotatedPassage
import com.quare.bibleplanner.ui.component.highlight.toBackgroundColor

private val tileSize = 44.dp
private val singleIconSize = 22.dp
private val multipleIconSize = 16.dp
private val inkOnHighlight = Color(0xB81A1B21)

@Composable
internal fun AnnotationTile(
    passage: AnnotatedPassage,
    modifier: Modifier = Modifier,
) {
    val highlightColor = passage.highlightColor
    val icons = listOfNotNull(
        Icons.Default.BorderColor.takeIf { highlightColor != null },
        Icons.Default.Bookmark.takeIf { passage.isSaved },
        Icons.Default.EditNote.takeIf { passage.note != null },
    )
    val iconSize = if (icons.size == 1) singleIconSize else multipleIconSize
    Box(
        modifier = modifier
            .size(tileSize)
            .background(
                color = highlightColor?.toBackgroundColor() ?: MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            icons.forEach { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint(
                        icon = icon,
                        isOnHighlight = highlightColor != null,
                    ),
                    modifier = Modifier.size(iconSize),
                )
            }
        }
    }
}

@Composable
private fun iconTint(
    icon: ImageVector,
    isOnHighlight: Boolean,
): Color = when {
    isOnHighlight -> inkOnHighlight
    icon == Icons.Default.Bookmark -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
