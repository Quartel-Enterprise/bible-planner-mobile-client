package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun BookPercentageText(
    progress: Float,
    bookIdName: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelSmall,
) {
    val percentage = (progress * 100).toInt()
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
    ) {
        with(sharedTransitionScope) {
            Text(
                text = percentage.toString(),
                style = style,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.sharedBounds(
                    rememberSharedContentState(key = "percentage-$bookIdName"),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
            )
        }
        with(sharedTransitionScope) {
            Text(
                text = "%",
                style = style.copy(fontSize = style.fontSize),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.sharedBounds(
                    rememberSharedContentState(key = "percentage-symbol-$bookIdName"),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
            )
        }
    }
}
