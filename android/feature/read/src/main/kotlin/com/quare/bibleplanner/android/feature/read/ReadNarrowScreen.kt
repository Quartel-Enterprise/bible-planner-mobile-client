package com.quare.bibleplanner.android.feature.read

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.DayReadingCompleteBanner
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.ui.utils.ReserveBottomOverlayHeightEffect
import com.quare.bibleplanner.ui.utils.asStable

// Why: the baseline profile journeys (tools/baseline-profile Journeys.kt) find and fling the verse list by this tag.
internal const val READ_CHAPTERS_TAG = "read_chapters"
private const val TITLE_VISIBLE_ITEM_INDEX = 1
private const val LINE_HEIGHT_RATIO = 1.75f
private val contentPadding = 20.dp
private val rulerContentPadding = 32.dp
private val bandOffsetBelowTopBar = 24.dp
private val bannerHorizontalPadding = 12.dp
private val bannerBottomSpacing = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadNarrowScreen(
    state: ReadUiState,
    listening: ReadListeningUiState,
    onEvent: (ReadUiEvent) -> Unit,
    onListeningEvent: (ReadListeningUiEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    // Why: the chapter skeletons pulse only while a neighbouring chapter is loading.
    val skeletonPulse = if (state.isLoadingPreviousChapter ||
        state.isLoadingNextChapter
    ) {
        rememberSkeletonPulse()
    } else {
        null
    }
    val density = LocalDensity.current
    val bottomBarScrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var bottomOverlayHeightPx by remember { mutableFloatStateOf(0f) }
    var miniPlayerHeightPx by remember { mutableFloatStateOf(0f) }
    val isTitleVisible by remember(listState) {
        derivedStateOf { listState.firstVisibleItemIndex >= TITLE_VISIBLE_ITEM_INDEX }
    }
    val listBindings = observeReadListBindings(
        state = state,
        listening = listening,
        listState = listState,
        onEvent = onEvent,
    )
    // Why: vertical reading drops the bottom bar, so its last measured height must not count anymore.
    val isBottomBarShown = !state.settings.isVerticalReadingEnabled
    val hasPlayer = listening.player != null
    // Why: the effect keeps its first lambda, so the values that change later are read through state.
    val currentIsBottomBarShown by rememberUpdatedState(isBottomBarShown)
    val currentHasPlayer by rememberUpdatedState(hasPlayer)
    ReserveBottomOverlayHeightEffect {
        val bottomBarHeight = if (currentIsBottomBarShown) {
            (bottomOverlayHeightPx + bottomBarScrollBehavior.state.heightOffset).coerceAtLeast(0f)
        } else {
            0f
        }
        bottomBarHeight + if (currentHasPlayer) miniPlayerHeightPx else 0f
    }
    var contentTopOffsetPx by remember { mutableFloatStateOf(0f) }
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(bottomBarScrollBehavior.nestedScrollConnection)
                .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
            topBar = {
                ReadTopBar(
                    header = state.header,
                    visibleChapter = listBindings.visibleChapter,
                    isTitleVisible = isTitleVisible,
                    isOpeningChapterStudy = state.isOpeningChapterStudy,
                    isChapterStudyBeside = state.isChapterStudyBeside,
                    topAppBarScrollBehavior = topBarScrollBehavior,
                    onEvent = onEvent,
                )
            },
            bottomBar = {
                Column {
                    listening.player?.let { player ->
                        val navigationBarPadding = WindowInsets.navigationBars
                            .asStable()
                            .asPaddingValues()
                            .calculateBottomPadding()
                        val hiddenBarFraction = if (state.settings.isVerticalReadingEnabled) {
                            1f
                        } else {
                            bottomBarScrollBehavior.state.collapsedFraction
                        }
                        // Why: the mini player stays when the bar hides, so it takes over the bar's inset as it goes.
                        ListeningMiniPlayerBar(
                            modifier = Modifier
                                .onSizeChanged { size -> miniPlayerHeightPx = size.height.toFloat() }
                                .padding(bottom = navigationBarPadding * hiddenBarFraction),
                            player = player,
                            onEvent = onListeningEvent,
                        )
                    }
                    /*
                     * Why: vertical reading has no single chapter to act on and each chapter ends with its
                     * own read pill, so the bar would name only the route's start chapter.
                     */
                    if (!state.settings.isVerticalReadingEnabled) {
                        ReadBottomBar(
                            modifier = Modifier.onSizeChanged { size -> bottomOverlayHeightPx = size.height.toFloat() },
                            header = state.header,
                            scrollBehavior = bottomBarScrollBehavior,
                            listening = listening,
                            onEvent = onEvent,
                            onListeningEvent = onListeningEvent,
                        )
                    }
                }
            },
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets.asStable(),
        ) { paddingValues ->
            /*
             * Why: scaffold padding shrinks to zero as the bars auto-hide; consuming it and
             * re-applying the remaining system-bar insets keeps text clear without double padding.
             */
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layout { measurable, constraints ->
                        // Why: read while laying out, so a collapsing top bar doesn't recompose the screen.
                        contentTopOffsetPx = paddingValues.calculateTopPadding().toPx()
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) {
                            placeable.place(
                                x = 0,
                                y = 0,
                            )
                        }
                    }.padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .windowInsetsPadding(WindowInsets.systemBars.asStable()),
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
                        val horizontalPadding = if (state.settings.isRulerEnabled) {
                            rulerContentPadding
                        } else {
                            contentPadding
                        }
                        LazyColumn(
                            modifier = Modifier
                                .testTag(READ_CHAPTERS_TAG)
                                .fillMaxSize()
                                .padding(horizontal = horizontalPadding),
                            state = listState,
                            contentPadding = PaddingValues(bottom = 16.dp),
                        ) {
                            if (state.isLoadingPreviousChapter) {
                                chapterSkeletonContent(
                                    position = ChapterSkeletonPosition.LEADING,
                                    pulse = { skeletonPulse?.value ?: 1f },
                                )
                            }
                            content.chapters.forEach { chapter ->
                                chapterContent(
                                    chapter = chapter,
                                    header = state.header,
                                    settings = state.settings,
                                    isChapterStudyBeside = state.isChapterStudyBeside,
                                    // Why: paged reading already has the bottom bar's listen button.
                                    isListenShortcutShown = state.settings.isVerticalReadingEnabled,
                                    focusedVerseNumber = null,
                                    verseFlash = listBindings.verseFlash,
                                    listening = listening,
                                    onEvent = onEvent,
                                    onListeningEvent = onListeningEvent,
                                )
                            }
                            if (state.isLoadingNextChapter) {
                                chapterSkeletonContent(
                                    position = ChapterSkeletonPosition.TRAILING,
                                    pulse = { skeletonPulse?.value ?: 1f },
                                )
                            }
                        }
                    }
                }
                ListeningOverlay(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    listening = listening,
                    follow = listBindings.listeningFollow,
                    onEvent = onEvent,
                    onListeningEvent = onListeningEvent,
                )
            }
        }
        /*
         * Why: drawn over the whole screen, not just the text, so the bars dim too and the
         * band is the only lit area.
         */
        if (state.settings.isRulerEnabled) {
            ReadingRulerOverlay(
                lineHeight = (state.settings.fontSizeSp * LINE_HEIGHT_RATIO).dp,
                lines = state.settings.rulerLines,
                initialBandOffset = {
                    with(density) { contentTopOffsetPx.toDp() } + bandOffsetBelowTopBar
                },
                onDismiss = { onEvent(ReadUiEvent.OnRulerDismissClick) },
            )
        }
        state.dayCompletionBanner?.let { day ->
            val bottomBarHeight = with(density) {
                val shownBarHeightPx = if (isBottomBarShown) bottomOverlayHeightPx else 0f
                (shownBarHeightPx + if (hasPlayer) miniPlayerHeightPx else 0f).toDp()
            }
            val navigationBarPadding = WindowInsets.navigationBars
                .asPaddingValues()
                .calculateBottomPadding()
            DayReadingCompleteBanner(
                day = day,
                onDismissRequest = { onEvent(ReadUiEvent.OnDayCompletionBannerDismissed) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = bannerHorizontalPadding)
                    .padding(bottom = maxOf(bottomBarHeight, navigationBarPadding) + bannerBottomSpacing),
            )
        }
    }
}
