package com.quare.bibleplanner.feature.read.presentation.screen.component.selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.add_note
import bibleplanner.feature.read.generated.resources.copy
import bibleplanner.feature.read.generated.resources.save_verses
import bibleplanner.feature.read.generated.resources.saved_verses
import bibleplanner.feature.read.generated.resources.share
import org.jetbrains.compose.resources.stringResource

private val tileHeight = 56.dp

@Composable
internal fun SelectionActionsRow(
    isSelectionSaved: Boolean,
    onSaveClick: () -> Unit,
    onNoteClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ActionTile(
            imageVector = if (isSelectionSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
            label = stringResource(if (isSelectionSaved) Res.string.saved_verses else Res.string.save_verses),
            isActive = isSelectionSaved,
            onClick = onSaveClick,
        )
        ActionTile(
            imageVector = Icons.Default.EditNote,
            label = stringResource(Res.string.add_note),
            isActive = false,
            onClick = onNoteClick,
        )
        ActionTile(
            imageVector = Icons.Default.Share,
            label = stringResource(Res.string.share),
            isActive = false,
            onClick = onShareClick,
        )
        ActionTile(
            imageVector = Icons.Default.ContentCopy,
            label = stringResource(Res.string.copy),
            isActive = false,
            onClick = onCopyClick,
        )
    }
}

@Composable
private fun RowScope.ActionTile(
    imageVector: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.weight(1f).height(tileHeight),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (isActive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
    ) {
        Column(
            modifier = Modifier.clickable(onClick = onClick),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}
