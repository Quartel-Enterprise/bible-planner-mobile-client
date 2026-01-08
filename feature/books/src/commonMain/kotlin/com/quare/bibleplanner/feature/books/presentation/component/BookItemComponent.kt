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
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BookItemComponent(
    book: BookPresentationModel,
    layoutFormat: BookLayoutFormat,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (layoutFormat == BookLayoutFormat.List) {
        BookListItem(
            book = book,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            onClick = onClick,
            onToggleFavorite = onToggleFavorite,
            modifier = modifier,
        )
    } else {
        BookGridItem(
            book = book,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            onClick = onClick,
            onToggleFavorite = onToggleFavorite,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun BookListItem(
    book: BookPresentationModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    with(sharedTransitionScope) {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .sharedElement(
                    rememberSharedContentState(key = "book-${book.id.name}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
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
                        imageVector = if (book.isCompleted) Icons.Default.Check else Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = if (book.isCompleted || book.progress > 0f) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                        modifier = Modifier.size(20.dp),
                    )
                }

                HorizontalSpacer(16.dp)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.id.getBookName(),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                            .clip(CircleShape),
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
                        .clickable { onToggleFavorite() },
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun BookGridItem(
    book: BookPresentationModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    with(sharedTransitionScope) {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .sharedElement(
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
                        .background(
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
                        modifier = Modifier.size(48.dp),
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
                                .clickable { onToggleFavorite() },
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
                    )

                    VerticalSpacer(4.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                            .clip(CircleShape),
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
