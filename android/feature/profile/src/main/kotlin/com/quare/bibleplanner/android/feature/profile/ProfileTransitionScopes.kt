package com.quare.bibleplanner.android.feature.profile

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.ui.utils.transition.sharedElementWithRelayout

/*
 * Why: these keys must match the paywall and release notes screens (native or Compose Multiplatform) for the
 * shared element transitions between the profile tab and them to keep running.
 */
internal const val BECOME_PRO_PART_1_KEY = "become_pro_part_1"
internal const val BECOME_PRO_PART_2_KEY = "become_pro_part_2"
internal const val RELEASE_NOTES_KEY = "release_notes_card"

internal class ProfileTransitionScopes(
    val sharedTransitionScope: SharedTransitionScope,
    val animatedVisibilityScope: AnimatedVisibilityScope,
)

@Composable
internal fun Modifier.sharedElementOf(
    transitionScopes: ProfileTransitionScopes?,
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
internal fun Modifier.sharedElementWithRelayoutOf(
    transitionScopes: ProfileTransitionScopes?,
    key: String,
): Modifier = if (transitionScopes == null) {
    this
} else {
    this.sharedElementWithRelayout(
        sharedTransitionScope = transitionScopes.sharedTransitionScope,
        animatedVisibilityScope = transitionScopes.animatedVisibilityScope,
        key = key,
    )
}
