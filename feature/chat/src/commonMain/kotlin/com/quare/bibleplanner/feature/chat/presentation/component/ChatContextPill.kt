package com.quare.bibleplanner.feature.chat.presentation.component

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
import androidx.compose.ui.unit.dp
import bibleplanner.feature.chat.generated.resources.Res
import bibleplanner.feature.chat.generated.resources.chat_context_empty
import bibleplanner.feature.chat.generated.resources.chat_context_loaded
import org.jetbrains.compose.resources.stringResource

private val pillShape = RoundedCornerShape(12.dp)
private val pillIconSize = 18.dp

@Composable
internal fun ChatContextPill(
    contextLabel: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (contextLabel == null) {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                } else {
                    MaterialTheme.colorScheme.primaryContainer
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
            imageVector = if (contextLabel == null) Icons.Rounded.AutoStories else Icons.Rounded.Bolt,
            contentDescription = null,
            modifier = Modifier.size(pillIconSize),
            tint = if (contextLabel == null) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            },
        )
        Text(
            text = contextLabel
                ?.let { stringResource(Res.string.chat_context_loaded, it) }
                ?: stringResource(Res.string.chat_context_empty),
            style = MaterialTheme.typography.labelLarge,
            color = if (contextLabel == null) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            },
        )
    }
}
