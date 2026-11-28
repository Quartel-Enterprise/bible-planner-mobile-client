package com.quare.bibleplanner.android.feature.main

import androidx.compose.animation.core.animate
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomAppBarState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/*
 * Why: exitAlwaysScrollBehavior, like the shared NarrowTabScaffold's, only moves the bar with the scroll a list
 * consumes, so a bar hidden over a list that then stops scrolling (a search that leaves a few books, a section
 * collapsed) or jumps in code would stay hidden. A downward drag that scrolls nothing and reveal() bring it back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
internal class MainBarScrollController(
    val state: BottomAppBarState,
    private val scope: CoroutineScope,
    behaviorConnection: NestedScrollConnection,
) {
    private var revealJob: Job? = null

    val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection by behaviorConnection {
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            // Why: a reveal still animating would otherwise write over the offset the user's scroll moves.
            if (source == NestedScrollSource.UserInput && consumed.y != 0f) revealJob?.cancel()
            val result = behaviorConnection.onPostScroll(
                consumed = consumed,
                available = available,
                source = source,
            )
            if (source == NestedScrollSource.UserInput && consumed.y == 0f && available.y > 0f) reveal()
            return result
        }
    }

    fun reveal() {
        if (state.heightOffset >= 0f || revealJob?.isActive == true) return
        revealJob = scope.launch {
            animate(
                initialValue = state.heightOffset,
                targetValue = 0f,
            ) { value, _ -> state.heightOffset = value }
        }
    }

    fun show() {
        revealJob?.cancel()
        state.heightOffset = 0f
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberMainBarScrollController(): MainBarScrollController {
    val scrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()
    val scope = rememberCoroutineScope()
    return remember(
        scrollBehavior,
        scope,
    ) {
        MainBarScrollController(
            state = scrollBehavior.state,
            scope = scope,
            behaviorConnection = scrollBehavior.nestedScrollConnection,
        )
    }
}
