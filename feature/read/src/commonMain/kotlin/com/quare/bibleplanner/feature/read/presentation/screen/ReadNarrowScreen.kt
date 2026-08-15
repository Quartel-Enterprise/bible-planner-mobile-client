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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quare.bibleplanner.core.provider.platform.Platform
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

private const val TITLE_VISIBLE_ITEM_INDEX = 1
private const val LINE_HEIGHT_RATIO = 1.75f

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
    ReserveBottomOverlayHeight {
        (bottomOverlayHeightPx + bottomBarScrollBehavior.state.heightOffset).coerceAtLeast(0f)
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(bottomBarScrollBehavior.nestedScrollConnection)
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        topBar = {
            ReadTopBar(
                platform = platform,
                header = state.header,
                isTitleVisible = isTitleVisible,
                topAppBarScrollBehavior = topBarScrollBehavior,
                onEvent = onEvent,
            )
        },
        bottomBar = {
            ReadBottomBar(
                modifier = Modifier.onSizeChanged { size -> bottomOverlayHeightPx = size.height.toFloat() },
                header = state.header,
                scrollBehavior = bottomBarScrollBehavior,
                onEvent = onEvent,
            )
        },
    ) { paddingValues ->
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
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
            if (state.settings.isRulerEnabled) {
                ReadingRulerOverlay(
                    lineHeight = (state.settings.fontSizeSp * LINE_HEIGHT_RATIO).dp,
                    onDismiss = { onEvent(ReadUiEvent.OnRulerDismissClick) },
                )
            }
        }
    }
}
