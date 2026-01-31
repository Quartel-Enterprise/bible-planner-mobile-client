package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun BookTypeIcon(
    book: BookPresentationModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp,
    shape: Shape = RoundedCornerShape(8.dp),
    iconAlpha: Float = 0.4f,
) {
    val backgroundColor = if (book.isCompleted) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (iconAlpha > 0.4f) 0.5f else 1.0f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (iconAlpha > 0.4f) 0.5f else 0.3f)
    }

    with(sharedTransitionScope) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .clip(shape)
                .sharedBounds(
                    rememberSharedContentState(key = "icon-box-${book.id.name}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                ).background(backgroundColor),
        ) {
            Icon(
                imageVector = if (book.isCompleted) Icons.Default.Check else Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = if (book.isCompleted || book.progress > 0f) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = iconAlpha)
                },
                modifier = Modifier
                    .size(iconSize)
                    .sharedElement(
                        rememberSharedContentState(key = "icon-${book.id.name}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    ),
            )
        }
    }
}
