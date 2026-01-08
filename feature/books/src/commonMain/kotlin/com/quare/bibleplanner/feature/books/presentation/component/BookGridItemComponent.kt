package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
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
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun BookGridItemComponent(
    modifier: Modifier = Modifier,
    book: BookPresentationModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onEvent: (BooksUiEvent) -> Unit,
) {
    with(sharedTransitionScope) {
        ElevatedCard(
            onClick = { onEvent(BooksUiEvent.OnBookClick(book)) },
            modifier = modifier
                .fillMaxWidth().sharedElement(
                    rememberSharedContentState(key = "book-${book.id.name}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(16.dp)),
                )
                .sharedBounds(
                    rememberSharedContentState(key = "book-${book.id.name}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Preview area / Icon area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .sharedBounds(
                            rememberSharedContentState(key = "icon-box-${book.id.name}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            clipInOverlayDuringTransition = OverlayClip(
                                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            ),
                        ).background(
                            if (book.isCompleted) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (book.isCompleted) Icons.Default.Check else Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = if (book.isCompleted || book.progress > 0f) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .sharedElement(
                                rememberSharedContentState(key = "icon-${book.id.name}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                            ),
                    )

                    // Favorite button positioned in the corner
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.TopEnd,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = stringResource(
                                Res.string.content_description_favorite,
                            ),
                            tint = if (book.isFavorite) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            },
                            modifier = Modifier
                                .size(20.dp)
                                .sharedElement(
                                    rememberSharedContentState(key = "favorite-${book.id.name}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                ).clickable { onEvent(BooksUiEvent.OnToggleFavorite(book.id)) },
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = book.id.getBookName(),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.sharedBounds(
                            rememberSharedContentState(key = "title-${book.id.name}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                        ),
                    )

                    VerticalSpacer(4.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .sharedBounds(
                                rememberSharedContentState(key = "progress-info-${book.id.name}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (book.isCompleted) {
                                stringResource(Res.string.completed)
                            } else {
                                book.chapterProgressText
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (book.isCompleted) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )

                        Text(
                            text = book.percentageText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    VerticalSpacer(8.dp)
                    LinearProgressIndicator(
                        progress = { if (book.isCompleted) 1f else book.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .height(8.dp)
                            .clip(CircleShape)
                            .sharedBounds(
                                rememberSharedContentState(key = "progress-bar-${book.id.name}"),
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
            }
        }
    }
}
