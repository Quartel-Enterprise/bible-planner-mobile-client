package com.quare.bibleplanner.feature.read.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.feature.read.presentation.screen.component.ReadBottomBar
import com.quare.bibleplanner.feature.read.presentation.screen.component.ReadTopBar
import com.quare.bibleplanner.feature.read.presentation.screen.component.ReadingRulerOverlay
import com.quare.bibleplanner.feature.read.presentation.screen.content.ReadErrorContent
import com.quare.bibleplanner.feature.read.presentation.screen.content.ReadLoadingContent
import com.quare.bibleplanner.feature.read.presentation.screen.content.chapterContent
import com.quare.bibleplanner.ui.utils.ReserveBottomOverlayHeight
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

private const val TITLE_VISIBLE_ITEM_INDEX = 1
private const val LINE_HEIGHT_RATIO = 1.75f
private const val PREFETCH_ITEM_COUNT = 6
private val contentPadding = 20.dp
private val rulerContentPadding = 32.dp
private val bandOffsetBelowTopBar = 24.dp
private const val CHAPTER_EXTRA_ITEM_COUNT = 2

/**
 * Which chapter the top of the list is sitting in. Each chapter lays out its header, one item per
 * verse and its end-of-chapter row, so the boundaries are a running total of those.
 */
@Composable
private fun rememberVisibleChapter(
    chapters: List<ReadChapterUiModel>,
    listState: LazyListState,
): ReadChapterUiModel? {
    val chapterStartIndices = remember(chapters) {
        chapters.runningFold(0) { start, chapter -> start + chapter.verses.size + CHAPTER_EXTRA_ITEM_COUNT }
    }
    return remember(chapters, chapterStartIndices) {
        derivedStateOf {
            chapters.indices
                .lastOrNull { index ->
                    chapterStartIndices[index] <= listState.firstVisibleItemIndex
                }?.let(chapters::get)
        }
    }.value
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadNarrowScreen(
    platform: Platform,
    state: ReadUiState,
    onEvent: (ReadUiEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    val bottomBarScrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var bottomOverlayHeightPx by remember { mutableFloatStateOf(0f) }
    val isTitleVisible by remember(listState) {
        derivedStateOf { listState.firstVisibleItemIndex >= TITLE_VISIBLE_ITEM_INDEX }
    }
    val chapters = (state.content as? ReadContentUiState.Success)?.chapters.orEmpty()
    val visibleChapter = rememberVisibleChapter(
        chapters = chapters,
        listState = listState,
    )
    /*
     * Asking one screen early keeps the next chapter arriving before the reader gets there, so
     * vertical reading reads as one continuous text rather than as a wait at every chapter end.
     */
    LaunchedEffect(listState, chapters) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index
        }.filterNotNull()
            .map { lastVisibleIndex -> lastVisibleIndex >= listState.layoutInfo.totalItemsCount - PREFETCH_ITEM_COUNT }
            .distinctUntilChanged()
            .filter { isNearEnd -> isNearEnd }
            .collect { onEvent(ReadUiEvent.OnReachedEnd) }
    }
    ReserveBottomOverlayHeight {
        (bottomOverlayHeightPx + bottomBarScrollBehavior.state.heightOffset).coerceAtLeast(0f)
    }
    var contentTopOffset by remember { mutableStateOf(0.dp) }
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(bottomBarScrollBehavior.nestedScrollConnection)
                .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
            topBar = {
                ReadTopBar(
                    platform = platform,
                    header = state.header,
                    visibleChapter = visibleChapter,
                    isTitleVisible = isTitleVisible,
                    topAppBarScrollBehavior = topBarScrollBehavior,
                    onEvent = onEvent,
                )
            },
            bottomBar = {
            /*
             * Vertical reading has no single chapter to act on, and every chapter already ends with
             * its own read pill, so the bar would only name whichever one the route started at.
             */
                if (!state.settings.isVerticalReadingEnabled) {
                    ReadBottomBar(
                        modifier = Modifier.onSizeChanged { size -> bottomOverlayHeightPx = size.height.toFloat() },
                        header = state.header,
                        scrollBehavior = bottomBarScrollBehavior,
                        onEvent = onEvent,
                    )
                }
            },
        ) { paddingValues ->
            contentTopOffset = paddingValues.calculateTopPadding()
        /*
         * The bars auto-hide as the reader scrolls, and the padding the scaffold hands out shrinks
         * with them — down to zero. Consuming that padding and then re-applying whatever system-bar
         * inset is left keeps the text clear of the status and navigation bars once the app's own
         * bars are gone, without double-padding while they are still on screen.
         */
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .windowInsetsPadding(WindowInsets.systemBars),
            ) {
                when (val content = state.content) {
                    ReadContentUiState.Loading -> ReadLoadingContent(Modifier.fillMaxSize())

                    is ReadContentUiState.Error -> {
                        ReadErrorContent(
                            modifier = Modifier.fillMaxSize(),
                            header = state.header,
                            content = content,
                            onEvent = onEvent,
                        )
                    }

                    is ReadContentUiState.Success -> {
                    /*
                     * While the ruler is on, the column gives up the width its grip and close button
                     * stand in, so a word reflows to the next line instead of sitting under them.
                     */
                        val horizontalPadding = if (state.settings.isRulerEnabled) {
                            rulerContentPadding
                        } else {
                            contentPadding
                        }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = horizontalPadding),
                            state = listState,
                            contentPadding = PaddingValues(bottom = 16.dp),
                        ) {
                            content.chapters.forEachIndexed { index, chapter ->
                                chapterContent(
                                    chapter = chapter,
                                    header = state.header,
                                    settings = state.settings,
                                    isPrimaryChapter = index == 0,
                                    focusedVerseNumber = null,
                                    onEvent = onEvent,
                                )
                            }
                        }
                    }
                }
            }
        }
        /*
         * Drawn over the whole screen rather than only the text, so the bars lose their prominence
         * with everything else: while the ruler is on, the band is the one lit thing.
         */
        if (state.settings.isRulerEnabled) {
            ReadingRulerOverlay(
                lineHeight = (state.settings.fontSizeSp * LINE_HEIGHT_RATIO).dp,
                lines = state.settings.rulerLines,
                initialBandOffset = contentTopOffset + bandOffsetBelowTopBar,
                onDismiss = { onEvent(ReadUiEvent.OnRulerDismissClick) },
            )
        }
    }
}
