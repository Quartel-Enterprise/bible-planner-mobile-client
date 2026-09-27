package com.quare.bibleplanner.feature.verse.annotations.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bibleplanner.feature.verse.annotations.generated.resources.Res
import bibleplanner.feature.verse.annotations.generated.resources.add_note
import bibleplanner.feature.verse.annotations.generated.resources.edit_note
import bibleplanner.feature.verse.annotations.generated.resources.more_options
import bibleplanner.feature.verse.annotations.generated.resources.open_in_chapter
import bibleplanner.feature.verse.annotations.generated.resources.remove
import bibleplanner.feature.verse.annotations.generated.resources.share
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationItemUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import com.quare.bibleplanner.ui.component.AppDropdownMenu
import com.quare.bibleplanner.ui.component.AppDropdownMenuItem
import com.quare.bibleplanner.ui.icons.AppIcon
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import com.quare.bibleplanner.ui.theme.font.toFontFamily
import org.jetbrains.compose.resources.stringResource

private const val TEXT_MAX_LINES = 2
private const val NOTE_MAX_LINES = 3

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
        Box {
            IconButton(onClick = { onEvent(AnnotationsUiEvent.OnItemMenuClick(item)) }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(Res.string.more_options),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AppDropdownMenu(
                isExpanded = isMenuOpen,
                onDismissRequest = { onEvent(AnnotationsUiEvent.OnItemMenuDismiss) },
                items = listOf(
                    AppDropdownMenuItem(
                        title = stringResource(Res.string.open_in_chapter),
                        icon = AppIcon.MenuBook,
                        isSelected = false,
                        isDestructive = false,
                        onClick = { onEvent(AnnotationsUiEvent.OnOpenInChapterClick(item)) },
                    ),
                    AppDropdownMenuItem(
                        title = stringResource(if (passage.note == null) Res.string.add_note else Res.string.edit_note),
                        icon = if (passage.note == null) AppIcon.EditNote else AppIcon.Edit,
                        isSelected = false,
                        isDestructive = false,
                        onClick = { onEvent(AnnotationsUiEvent.OnNoteClick(item)) },
                    ),
                    AppDropdownMenuItem(
                        title = stringResource(Res.string.share),
                        icon = AppIcon.Share,
                        isSelected = false,
                        isDestructive = false,
                        onClick = { onEvent(AnnotationsUiEvent.OnShareClick(item)) },
                    ),
                    AppDropdownMenuItem(
                        title = stringResource(Res.string.remove),
                        icon = AppIcon.Delete,
                        isSelected = false,
                        isDestructive = true,
                        onClick = { onEvent(AnnotationsUiEvent.OnRemoveClick(item)) },
                    ),
                ),
            )
        }
    }
}
