package com.quare.bibleplanner.android.feature.annotations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.core.verseannotations.domain.model.AnnotatedPassage
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationItemUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import com.quare.bibleplanner.ui.component.highlight.toBackgroundColor
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import com.quare.bibleplanner.ui.theme.font.toFontFamily

private const val TEXT_MAX_LINES = 2
private const val NOTE_MAX_LINES = 3
private val tileSize = 44.dp
private val singleIconSize = 22.dp
private val multipleIconSize = 16.dp
private val inkOnHighlight = Color(0xB81A1B21)

@Composable
internal fun AnnotationItemRow(
    item: AnnotationItemUiModel,
    isMenuOpen: Boolean,
    isWide: Boolean,
    onEvent: (AnnotationsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val passage = item.passage
    Row(
        modifier = modifier
            .clickable { onEvent(AnnotationsUiEvent.OnItemClick(item)) }
            .padding(
                start = 14.dp,
                end = 4.dp,
                top = 12.dp,
                bottom = 12.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnnotationTile(passage = passage)
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = verseReferenceLabel(
                        bookId = passage.chapter.bookId,
                        chapterNumber = passage.chapter.chapterNumber,
                        verseNumbers = passage.verseNumbers,
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text(
                    text = "· ${item.versionAbbreviation}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            if (item.text.isNotBlank()) {
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = ReaderFont.LORA.toFontFamily(),
                    maxLines = if (isWide) Int.MAX_VALUE else TEXT_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            passage.note?.let { note ->
                Text(
                    text = "“${note.text}”",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = NOTE_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        AnnotationItemMenu(
            item = item,
            isMenuOpen = isMenuOpen,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun AnnotationItemMenu(
    item: AnnotationItemUiModel,
    isMenuOpen: Boolean,
    onEvent: (AnnotationsUiEvent) -> Unit,
) {
    val hasNote = item.passage.note != null
    Box {
        IconButton(onClick = { onEvent(AnnotationsUiEvent.OnItemMenuClick(item)) }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.annotations_more_options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(
            expanded = isMenuOpen,
            onDismissRequest = { onEvent(AnnotationsUiEvent.OnItemMenuDismiss) },
        ) {
            AnnotationItemMenuOption(
                title = stringResource(R.string.annotations_open_in_chapter),
                icon = Icons.AutoMirrored.Filled.MenuBook,
                onClick = { onEvent(AnnotationsUiEvent.OnOpenInChapterClick(item)) },
            )
            AnnotationItemMenuOption(
                title = stringResource(if (hasNote) R.string.annotations_edit_note else R.string.annotations_add_note),
                icon = if (hasNote) Icons.Default.Edit else Icons.Default.EditNote,
                onClick = { onEvent(AnnotationsUiEvent.OnNoteClick(item)) },
            )
            AnnotationItemMenuOption(
                title = stringResource(R.string.annotations_share),
                icon = Icons.Default.Share,
                onClick = { onEvent(AnnotationsUiEvent.OnShareClick(item)) },
            )
            AnnotationItemMenuOption(
                title = stringResource(R.string.annotations_remove),
                icon = Icons.Default.Delete,
                isDestructive = true,
                onClick = { onEvent(AnnotationsUiEvent.OnRemoveClick(item)) },
            )
        }
    }
}

@Composable
private fun AnnotationItemMenuOption(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.error else LocalContentColor.current
    DropdownMenuItem(
        text = {
            Text(
                text = title,
                color = contentColor,
            )
        },
        onClick = onClick,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
            )
        },
    )
}

@Composable
private fun AnnotationTile(passage: AnnotatedPassage) {
    val highlightColor = passage.highlightColor
    val icons = listOfNotNull(
        Icons.Default.BorderColor.takeIf { highlightColor != null },
        Icons.Default.Bookmark.takeIf { passage.isSaved },
        Icons.Default.EditNote.takeIf { passage.note != null },
    )
    val iconSize = if (icons.size == 1) singleIconSize else multipleIconSize
    Box(
        modifier = Modifier
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
                    tint = when {
                        highlightColor != null -> inkOnHighlight
                        icon == Icons.Default.Bookmark -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(iconSize),
                )
            }
        }
    }
}
