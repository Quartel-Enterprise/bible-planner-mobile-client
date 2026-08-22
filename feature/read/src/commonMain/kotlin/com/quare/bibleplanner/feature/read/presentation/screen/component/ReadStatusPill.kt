package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.mark_as_read
import bibleplanner.feature.read.generated.resources.read_status_read
import org.jetbrains.compose.resources.stringResource

private val pillHeight = 44.dp
private val compactPillHeight = 34.dp
private val iconSize = 18.dp

/**
 * The chapter's read state as a single pill that flips between an outlined "mark as read" and a
 * filled "read", so the two states never look like two different controls.
 */
@Composable
internal fun ReadStatusPill(
    isRead: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    label: String? = null,
) {
    val containerColor = if (isRead) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val contentColor = if (isRead) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Button(
        modifier = modifier.height(if (isCompact) compactPillHeight else pillHeight),
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isRead) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
        ),
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                imageVector = if (isRead) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Outlined.RadioButtonUnchecked
                },
                contentDescription = null,
            )
            Text(
                text = label ?: stringResource(
                    if (isRead) Res.string.read_status_read else Res.string.mark_as_read,
                ),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
