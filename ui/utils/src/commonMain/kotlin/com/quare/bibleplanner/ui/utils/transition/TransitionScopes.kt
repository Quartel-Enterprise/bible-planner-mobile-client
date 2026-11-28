package com.quare.bibleplanner.ui.utils.transition

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/*
 * Why: the helpers below take it nullable, so a preview, or a host without a shared transition scope, skips the
 * modifier instead of crashing.
 */
class TransitionScopes(
    val sharedTransitionScope: SharedTransitionScope,
    val animatedVisibilityScope: AnimatedVisibilityScope,
)

@Composable
fun Modifier.sharedElementOf(
    transitionScopes: TransitionScopes?,
    key: Any,
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
fun Modifier.sharedBoundsOf(
    transitionScopes: TransitionScopes?,
    key: Any,
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

// Why: for an element in a TopAppBar, which never re-places a shared element that ends early (CMP-10888).
@Composable
fun Modifier.sharedElementWithRelayoutOf(
    transitionScopes: TransitionScopes?,
    key: Any,
): Modifier = if (transitionScopes == null) {
    this
} else {
    sharedElementWithRelayout(
        sharedTransitionScope = transitionScopes.sharedTransitionScope,
        animatedVisibilityScope = transitionScopes.animatedVisibilityScope,
        key = key,
    )
}
