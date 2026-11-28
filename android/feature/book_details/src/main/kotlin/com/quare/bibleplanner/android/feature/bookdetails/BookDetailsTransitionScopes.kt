package com.quare.bibleplanner.android.feature.bookdetails

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/*
 * Why: these keys must match the books tab's and the reader's (native or Compose Multiplatform) for the shared
 * element transitions to keep running between them and this native screen.
 */
internal class BookDetailsTransitionScopes(
    val sharedTransitionScope: SharedTransitionScope,
    val animatedVisibilityScope: AnimatedVisibilityScope,
)

@Composable
internal fun Modifier.sharedElementOf(
    transitionScopes: BookDetailsTransitionScopes?,
    key: String,
): Modifier = if (transitionScopes == null) {
    this
} else {
    with(transitionScopes.sharedTransitionScope) {
        this@sharedElementOf.sharedElement(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = transitionScopes.animatedVisibilityScope,
        )
    }
}

@Composable
internal fun Modifier.sharedBoundsOf(
    transitionScopes: BookDetailsTransitionScopes?,
    key: String,
): Modifier = if (transitionScopes == null) {
    this
} else {
    with(transitionScopes.sharedTransitionScope) {
        this@sharedBoundsOf.sharedBounds(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = transitionScopes.animatedVisibilityScope,
        )
    }
}
