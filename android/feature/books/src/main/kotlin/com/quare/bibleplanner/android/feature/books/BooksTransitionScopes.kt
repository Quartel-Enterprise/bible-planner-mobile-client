package com.quare.bibleplanner.android.feature.books

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/*
 * Why: these keys must match the book details and reader screens (native or Compose Multiplatform) for the
 * shared element transitions between the books tab and them to keep running.
 */
internal class BooksTransitionScopes(
    val sharedTransitionScope: SharedTransitionScope,
    val animatedVisibilityScope: AnimatedVisibilityScope,
)

@Composable
internal fun Modifier.sharedElementOf(
    transitionScopes: BooksTransitionScopes?,
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
    transitionScopes: BooksTransitionScopes?,
    key: String,
    clipInOverlayDuringTransition: SharedTransitionScope.OverlayClip? = null,
): Modifier = if (transitionScopes == null) {
    this
} else {
    with(transitionScopes.sharedTransitionScope) {
        val sharedContentState = rememberSharedContentState(key = key)
        if (clipInOverlayDuringTransition == null) {
            this@sharedBoundsOf.sharedBounds(
                sharedContentState = sharedContentState,
                animatedVisibilityScope = transitionScopes.animatedVisibilityScope,
            )
        } else {
            this@sharedBoundsOf.sharedBounds(
                sharedContentState = sharedContentState,
                animatedVisibilityScope = transitionScopes.animatedVisibilityScope,
                clipInOverlayDuringTransition = clipInOverlayDuringTransition,
            )
        }
    }
}
