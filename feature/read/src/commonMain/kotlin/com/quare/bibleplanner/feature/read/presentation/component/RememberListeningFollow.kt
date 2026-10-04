package com.quare.bibleplanner.feature.read.presentation.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.VerseFocusUiModel
import com.quare.bibleplanner.feature.read.presentation.screen.findVerseItemIndex
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private const val VERSE_TOP_FRACTION = 0.3f

/*
 * Why: the list follows the verse being read until the person scrolls it away themselves; a
 * programmatic scroll never moves the bars' nested scroll, so only a real drag can stop following.
 */
@Composable
internal fun rememberListeningFollow(
    player: ListeningPlayerUiModel?,
    chapters: List<ReadChapterUiModel>,
    listState: LazyListState,
    leadingItemCount: Int,
    isChapterStudyBeside: Boolean,
): ListeningFollow {
    var isFollowing by remember { mutableStateOf(true) }
    var isAutoScrolling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val currentChapters by rememberUpdatedState(chapters)
    val currentLeadingItemCount by rememberUpdatedState(leadingItemCount)
    val currentIsChapterStudyBeside by rememberUpdatedState(isChapterStudyBeside)
    val isTracking = player != null &&
        player.verseNumber != null &&
        player.status != ListeningStatusModel.FINISHED
    val verseItemIndex = if (isTracking) {
        findVerseItemIndex(
            chapters = chapters,
            leadingItemCount = leadingItemCount,
            isChapterStudyBeside = isChapterStudyBeside,
            focus = VerseFocusUiModel(
                bookId = player.chapter.bookId,
                chapterNumber = player.chapter.chapterNumber,
                verseNumbers = listOfNotNull(player.verseNumber),
            ),
        )
    } else {
        null
    }
    val currentVerseItemIndex by rememberUpdatedState(verseItemIndex)

    suspend fun scrollToVerse(index: Int) {
        isAutoScrolling = true
        val viewportHeight = listState.layoutInfo.viewportSize.height
        // Why: a drag interrupts the animation with a cancellation, and the flag must not outlive it.
        try {
            listState.animateScrollToItem(
                index = index,
                scrollOffset = -(viewportHeight * VERSE_TOP_FRACTION).toInt(),
            )
        } finally {
            isAutoScrolling = false
        }
    }

    LaunchedEffect(player == null) {
        if (player == null) isFollowing = true
    }
    LaunchedEffect(verseItemIndex) {
        val index = verseItemIndex ?: return@LaunchedEffect
        if (isFollowing) scrollToVerse(index)
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { isScrolling ->
                if (isScrolling || isAutoScrolling) return@collect
                val index = currentVerseItemIndex ?: return@collect
                isFollowing = listState.isItemFullyVisible(index)
            }
    }
    val isVerseAbove by remember(listState) {
        derivedStateOf { (currentVerseItemIndex ?: 0) < listState.firstVisibleItemIndex }
    }
    return ListeningFollow(
        isShowingBackToVerse = !isFollowing && verseItemIndex != null,
        isVerseAbove = isVerseAbove,
        backToVerse = {
            isFollowing = true
            currentVerseItemIndex?.let { index -> scope.launch { scrollToVerse(index) } }
        },
    )
}

private fun LazyListState.isItemFullyVisible(index: Int): Boolean {
    val item = layoutInfo.visibleItemsInfo.find { it.index == index } ?: return false
    return item.offset >= layoutInfo.viewportStartOffset &&
        item.offset + item.size <= layoutInfo.viewportEndOffset
}
