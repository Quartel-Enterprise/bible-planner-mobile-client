package com.quare.bibleplanner.feature.chat.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val chipShape = RoundedCornerShape(18.dp)
private val chipBorderWidth = 1.dp

/**
 * The chat's own suggestion chip rather than Material's, whose height is fixed at 32dp: these read
 * as questions, not as filters, and need the room to breathe that the design gives them.
 */
@Composable
internal fun ChatSuggestionChip(
    suggestion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = chipShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = chipBorderWidth,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Text(
            text = suggestion,
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 10.dp,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
