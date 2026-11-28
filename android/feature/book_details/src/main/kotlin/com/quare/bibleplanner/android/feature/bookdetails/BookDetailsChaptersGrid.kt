package com.quare.bibleplanner.android.feature.bookdetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.ui.utils.SharedTransitionModifierFactory

// Why: 50dp fits six chapters per row with the spacing.
private val chapterItemWidth = 50.dp

@Composable
internal fun BookDetailsChaptersGrid(
    bookName: String,
    chapters: List<BookChapterModel>,
    areAllChaptersRead: Boolean,
    transitionScopes: BookDetailsTransitionScopes?,
    onChapterClick: (Int) -> Unit,
    onToggleAllClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.book_details_chapters),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            TextButton(onClick = onToggleAllClick) {
                Text(
                    text = stringResource(
                        if (areAllChaptersRead) {
                            R.string.book_details_mark_all_as_unread
                        } else {
                            R.string.book_details_mark_all_as_read
                        },
                    ),
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            chapters.forEach { chapter ->
                ChapterItem(
                    bookName = bookName,
                    chapter = chapter,
                    transitionScopes = transitionScopes,
                    onClick = { onChapterClick(chapter.number) },
                )
            }
        }
    }
}

@Composable
private fun ChapterItem(
    bookName: String,
    chapter: BookChapterModel,
    transitionScopes: BookDetailsTransitionScopes?,
    onClick: () -> Unit,
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
        modifier = Modifier
            .width(chapterItemWidth)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = chapter.number.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = contentColor,
            modifier = if (transitionScopes == null) {
                Modifier
            } else {
                SharedTransitionModifierFactory.getReadTopBarSharedTransitionBookChapterModifier(
                    sharedTransitionScope = transitionScopes.sharedTransitionScope,
                    animatedVisibilityScope = transitionScopes.animatedVisibilityScope,
                    chapterNumber = chapter.number,
                    bookName = bookName,
                )
            },
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
