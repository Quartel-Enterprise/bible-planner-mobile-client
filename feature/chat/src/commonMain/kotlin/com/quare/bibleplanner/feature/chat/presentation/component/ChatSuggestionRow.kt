package com.quare.bibleplanner.feature.chat.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val rowShape = RoundedCornerShape(12.dp)
private val rowBorderWidth = 1.dp
private val arrowSize = 16.dp
private const val MAX_LINES = 2

@Composable
internal fun ChatSuggestionRow(
    suggestion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = rowShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(
            width = rowBorderWidth,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 9.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = suggestion,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                imageVector = Icons.Rounded.NorthEast,
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(arrowSize),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
