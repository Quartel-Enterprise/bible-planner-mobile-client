package com.quare.bibleplanner.feature.bookdetails.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.book_details.generated.resources.Res
import bibleplanner.feature.book_details.generated.resources.chapters
import bibleplanner.feature.book_details.generated.resources.mark_all_as_read
import bibleplanner.feature.book_details.generated.resources.mark_all_as_unread
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChaptersGrid(
    chapters: List<BookChapterModel>,
    areAllChaptersRead: Boolean,
    onChapterClick: (Int) -> Unit,
    onToggleAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(Res.string.chapters),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(
                    if (areAllChaptersRead) Res.string.mark_all_as_unread else Res.string.mark_all_as_read,
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleAllClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        VerticalSpacer(16)

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 6,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val itemWidth = 50.dp // Approximate width to fit 6 items with spacing

            chapters.forEach { chapter ->
                ChapterItem(
                    chapter = chapter,
                    onClick = { onChapterClick(chapter.number) },
                    modifier = Modifier.width(itemWidth),
                )
            }
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: BookChapterModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (chapter.isRead) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        },
        label = "chapterBackgroundAnimation",
    )

    val contentColor by animateColorAsState(
        targetValue = if (chapter.isRead) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        },
        label = "chapterContentAnimation",
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = chapter.number.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = contentColor,
        )

        AnimatedVisibility(
            visible = chapter.isRead,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}
