package com.quare.bibleplanner.feature.chat.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bibleplanner.feature.chat.generated.resources.Res
import bibleplanner.feature.chat.generated.resources.chat_context_chip
import org.jetbrains.compose.resources.stringResource

private val chipShape = RoundedCornerShape(20.dp)
private val chipIconSize = 17.dp

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
            text = stringResource(Res.string.chat_context_chip, contextLabel),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
