package com.quare.bibleplanner.android.feature.day

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.ui.utils.transition.sharedElementWithRelayout

/*
 * Why: the plans tab (native or Compose Multiplatform) declares the same keys (SharedTransitionAnimationUtils), so
 * the week title, the day title and the planned date keep flying between it and this native screen.
 */
internal class DayTransitionScopes(
    val sharedTransitionScope: SharedTransitionScope,
    val animatedVisibilityScope: AnimatedVisibilityScope,
)

@Composable
internal fun Modifier.sharedElementOf(
    transitionScopes: DayTransitionScopes?,
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

// Why: the title sits in a TopAppBar, which never re-places a shared element that ends early (CMP-10888).
@Composable
internal fun Modifier.titleSharedElementOf(
    transitionScopes: DayTransitionScopes?,
    key: String,
): Modifier = if (transitionScopes == null) {
    this
} else {
    sharedElementWithRelayout(
        sharedTransitionScope = transitionScopes.sharedTransitionScope,
        animatedVisibilityScope = transitionScopes.animatedVisibilityScope,
        key = key,
    )
}
