package com.quare.bibleplanner.ui.testing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Shows the source until isTargetShown turns true, then the target, in one shared transition. The
// source sits at the bottom start, far from a target's top bar, so a target left at the source's
// position ends up outside it.
@Composable
fun SharedTransitionTestContent(
    isTargetShown: Boolean,
    source: @Composable SharedTransitionScope.(AnimatedContentScope) -> Unit,
    target: @Composable SharedTransitionScope.(AnimatedContentScope) -> Unit,
) {
    SharedTransitionLayout {
        AnimatedContent(targetState = isTargetShown) { isTarget ->
            if (isTarget) {
                this@SharedTransitionLayout.target(this@AnimatedContent)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = 24.dp),
                    contentAlignment = Alignment.BottomStart,
                ) {
                    this@SharedTransitionLayout.source(this@AnimatedContent)
                }
            }
        }
    }
}
