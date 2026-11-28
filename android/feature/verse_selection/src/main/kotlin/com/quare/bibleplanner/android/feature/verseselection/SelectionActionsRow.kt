package com.quare.bibleplanner.android.feature.verseselection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiEvent

private val tileHeight = 56.dp

@Composable
internal fun SelectionActionsRow(
    isSelectionSaved: Boolean,
    hasNote: Boolean,
    onEvent: (VerseSelectionUiEvent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SelectionActionTile(
            imageVector = if (isSelectionSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
            label = stringResource(
                if (isSelectionSaved) R.string.verse_selection_saved else R.string.verse_selection_save,
            ),
            isActive = isSelectionSaved,
            onClick = { onEvent(VerseSelectionUiEvent.OnToggleSavedClick) },
        )
        SelectionActionTile(
            imageVector = if (hasNote) Icons.AutoMirrored.Filled.StickyNote2 else Icons.Default.EditNote,
            label = stringResource(
                if (hasNote) R.string.verse_selection_view_note else R.string.verse_selection_add_note,
            ),
            isActive = hasNote,
            onClick = { onEvent(VerseSelectionUiEvent.OnNoteClick) },
        )
        SelectionActionTile(
            imageVector = Icons.Default.Share,
            label = stringResource(R.string.verse_selection_share),
            isActive = false,
            onClick = { onEvent(VerseSelectionUiEvent.OnShareClick) },
        )
        SelectionActionTile(
            imageVector = Icons.Default.ContentCopy,
            label = stringResource(R.string.verse_selection_copy),
            isActive = false,
            onClick = { onEvent(VerseSelectionUiEvent.OnCopyClick) },
        )
    }
}

@Composable
private fun RowScope.SelectionActionTile(
    imageVector: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .height(tileHeight),
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
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
