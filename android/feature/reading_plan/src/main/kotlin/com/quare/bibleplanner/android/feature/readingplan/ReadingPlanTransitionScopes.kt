package com.quare.bibleplanner.android.feature.readingplan

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/*
 * Why: the Compose Multiplatform day screen declares the same keys (SharedTransitionAnimationUtils), so the week
 * title, the day title and the planned date keep flying between this tab and the day.
 */
internal class ReadingPlanTransitionScopes(
    val sharedTransitionScope: SharedTransitionScope,
    val animatedVisibilityScope: AnimatedVisibilityScope,
)

@Composable
internal fun Modifier.sharedElementOf(
    transitionScopes: ReadingPlanTransitionScopes?,
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
