package com.quare.bibleplanner.android.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val chipShape = RoundedCornerShape(20.dp)
private val chipIconSize = 17.dp
private val pillShape = RoundedCornerShape(12.dp)
private val pillIconSize = 18.dp

@Composable
internal fun ChatContextChip(
    contextLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = chipShape,
            ).padding(
                horizontal = 14.dp,
                vertical = 8.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Bolt,
            contentDescription = null,
            modifier = Modifier.size(chipIconSize),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = stringResource(R.string.chat_context_chip, contextLabel),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun ChatContextPill(
    contextLabel: String?,
    modifier: Modifier = Modifier,
) {
    val hasContext = contextLabel != null
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (hasContext) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                shape = pillShape,
            ).padding(
                horizontal = 12.dp,
                vertical = 9.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (hasContext) Icons.Rounded.Bolt else Icons.Rounded.AutoStories,
            contentDescription = null,
            modifier = Modifier.size(pillIconSize),
            tint = if (hasContext) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.primary
            },
        )
        Text(
            text = contextLabel
                ?.let { stringResource(R.string.chat_context_loaded, it) }
                ?: stringResource(R.string.chat_context_empty),
            style = MaterialTheme.typography.labelLarge,
            color = if (hasContext) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
