package com.quare.bibleplanner.android.feature.chat

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Composable
internal fun ChatScrollEffect(
    requests: Flow<Unit>,
    listState: LazyListState,
    hasThread: Boolean,
    isAnswering: Boolean,
) {
    var hasLanded by rememberSaveable { mutableStateOf(false) }
    var isFollowing by remember { mutableStateOf(true) }
    /*
     * Why: driven by the thread itself because the view model's scroll request is a one-shot event
     * the screen is not yet collecting when a thread opens.
     */
    LaunchedEffect(hasThread) {
        if (!hasThread || hasLanded) return@LaunchedEffect
        snapshotFlow { listState.layoutInfo.totalItemsCount }.first { count -> count > 0 }
        listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1)
        listState.settleAtEnd()
        hasLanded = true
    }
    /*
     * Why: a drag releases the end and returning to it takes hold again; streaming only follows while
     * held, so an answer being written never drags the list from under someone reading above.
     */
    LaunchedEffect(listState) {
        listState.interactionSource.interactions.collect { interaction ->
            if (interaction is DragInteraction.Start) isFollowing = false
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collect { isScrolling ->
            if (!isScrolling) isFollowing = listState.getEndOvershoot() <= 0f
        }
    }
    LaunchedEffect(
        listState,
        isAnswering,
    ) {
        if (!isAnswering) return@LaunchedEffect
        isFollowing = true
        snapshotFlow { listState.getEndOvershoot() }.collect { overshoot ->
            if (isFollowing && overshoot > 0f) listState.ignoringInterruption { scrollBy(overshoot) }
        }
    }
    LaunchedEffect(requests) {
        requests.collect {
            if (isFollowing) listState.ignoringInterruption { animateToEnd() }
        }
    }
}

/*
 * Why: a manual or newer scroll cancels the one in flight; that cancellation must not reach the
 * collector or it would end for the screen's life and no later answer would reach the end.
 */
private suspend fun LazyListState.ignoringInterruption(scroll: suspend LazyListState.() -> Unit) {
    try {
        scroll()
    } catch (interruption: CancellationException) {
        currentCoroutineContext().ensureActive()
        Logger.d(interruption) { "A scroll to the end of the chat was interrupted" }
    }
}

/*
 * Why: scrolling to the last item leaves its start at the top, which for an answer taller than
 * the screen is far from the end, so the remaining overshoot is scrolled too.
 */
private suspend fun LazyListState.animateToEnd() {
    val lastIndex = layoutInfo.totalItemsCount - 1
    if (lastIndex < 0) return
    animateScrollToItem(lastIndex)
    val overshoot = getEndOvershoot()
    if (overshoot > 0f) animateScrollBy(overshoot)
    settleAtEnd()
}

private suspend fun LazyListState.settleAtEnd() {
    val overshoot = getEndOvershoot()
    if (overshoot > 0f) scrollBy(overshoot)
}

private fun LazyListState.getEndOvershoot(): Float {
    val last = layoutInfo.visibleItemsInfo.lastOrNull() ?: return 0f
    return (last.offset + last.size - layoutInfo.viewportEndOffset).toFloat()
}
