package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.completed
import bibleplanner.feature.books.generated.resources.content_description_favorite
import bibleplanner.feature.books.generated.resources.content_description_navigate
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
fun BookItemComponent(
    book: BookPresentationModel,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = {
                Text(
                    text = book.id.getBookName(),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            supportingContent = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (book.isCompleted) {
                            Text(
                                text = stringResource(Res.string.completed),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            Text(
                                text = book.chapterProgressText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        HorizontalSpacer(8.dp)

                        Text(
                            text = if (book.isCompleted) "100%" else book.percentageText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f).wrapContentWidth(Alignment.End),
                        )
                    }

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
            },
            leadingContent = {
                val backgroundColor = if (book.isCompleted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(backgroundColor),
                ) {
                    CircularProgressIndicator(
                        progress = { book.progress },
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round,
                    )

                    val icon = if (book.isCompleted) Icons.Default.Check else Icons.Default.MenuBook
                    val iconTint = if (book.isCompleted || book.progress > 0f) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null, // Decorative icon inside progress
                        tint = iconTint,
                        modifier = Modifier.size(24.dp),
                    )
                }
            },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = stringResource(Res.string.content_description_favorite),
                        tint = if (book.isFavorite) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.5f,
                            )
                        }, // Adjusted for favorite state logic
                        modifier = Modifier.clickable { onToggleFavorite() },
                    )

                    HorizontalSpacer(16.dp)
                }
            },
        )
    }
}
