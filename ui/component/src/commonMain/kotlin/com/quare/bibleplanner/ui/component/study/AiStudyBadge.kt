package com.quare.bibleplanner.ui.component.study

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val ACCENT_CONTAINER_ALPHA = 0.12f

@Composable
fun AiStudyBadge(
    text: String,
    isAccent: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = if (isAccent) {
            MaterialTheme.colorScheme.primary.copy(alpha = ACCENT_CONTAINER_ALPHA)
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (isAccent) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 6.dp,
                vertical = 2.dp,
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}
