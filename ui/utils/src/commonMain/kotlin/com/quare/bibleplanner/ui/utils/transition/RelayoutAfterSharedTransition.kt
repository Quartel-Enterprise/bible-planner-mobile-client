package com.quare.bibleplanner.ui.utils.transition

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.ui.Modifier

// Why: CMP-10888, in Compose 1.12.1 a shared element whose transition ends within about 2 frames (a
// slow first frame, or animations turned off) is re-measured but never re-placed, so it stays at the
// source's position; the relayout requested in that same frame is dropped, hence the one-frame delay.
fun Modifier.relayoutAfterSharedTransition(sharedTransitionScope: SharedTransitionScope): Modifier =
    this then RelayoutAfterSharedTransitionElement(sharedTransitionScope)
