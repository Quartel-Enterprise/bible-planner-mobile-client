package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.completed
import bibleplanner.feature.books.generated.resources.content_description_favorite
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.utils.getShadowClip
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun BookListItemComponent(
    book: BookPresentationModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onEvent: (BooksUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    with(sharedTransitionScope) {
        val bookIdName = book.id.name
        ElevatedCard(
            onClick = { onEvent(BooksUiEvent.OnBookClick(book)) },
            modifier = modifier
                .fillMaxWidth()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "book-$bookIdName"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    clipInOverlayDuringTransition = getShadowClip(RoundedCornerShape(16.dp)),
                ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val backgroundColor = if (book.isCompleted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor),
                ) {
                    Icon(
                        imageVector = if (book.isCompleted) Icons.Default.Check else Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = if (book.isCompleted || book.progress > 0f) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                        modifier = Modifier
                            .size(20.dp)
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = "icon-$bookIdName"),
                                animatedVisibilityScope = animatedVisibilityScope,
                            ),
                    )
                }

                HorizontalSpacer(16.dp)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.id.getBookName(),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "title-$bookIdName"),
                            animatedVisibilityScope = animatedVisibilityScope,
                        ),
                    )

                    Row(
                        modifier = Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "progress-info-$bookIdName"),
                            animatedVisibilityScope = animatedVisibilityScope,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (book.isCompleted) {
                                stringResource(Res.string.completed)
                            } else {
                                book.chapterProgressText
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (book.isCompleted) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )

                        HorizontalSpacer(8.dp)
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                ),
                        )
                        HorizontalSpacer(8.dp)
                        Text(
                            text = book.percentageText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    VerticalSpacer(8.dp)
                    LinearProgressIndicator(
                        progress = { book.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape)
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "progress-bar-$bookIdName"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                clipInOverlayDuringTransition = OverlayClip(CircleShape),
                            ),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round,
                        gapSize = (-15).dp,
                        drawStopIndicator = {},
                    )
                }

                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = stringResource(Res.string.content_description_favorite),
                    tint = if (book.isFavorite) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "favorite-$bookIdName"),
                            animatedVisibilityScope = animatedVisibilityScope,
                        ).clickable { onEvent(BooksUiEvent.OnToggleFavorite(book.id)) },
                )
            }
        }
    }
}
