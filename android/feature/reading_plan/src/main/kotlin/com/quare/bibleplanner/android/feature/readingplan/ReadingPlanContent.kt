package com.quare.bibleplanner.android.feature.readingplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMode
import com.quare.bibleplanner.feature.readingplan.presentation.content.COLLAPSED_WEEK_LIMIT
import com.quare.bibleplanner.feature.readingplan.presentation.content.ReadingPlanListItem
import com.quare.bibleplanner.feature.readingplan.presentation.content.buildReadingPlanWeekItems
import com.quare.bibleplanner.feature.readingplan.presentation.content.getReadingPlanItemKey
import com.quare.bibleplanner.feature.readingplan.presentation.content.getReadingPlanWeekScrollIndex
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekGroup
import com.quare.bibleplanner.ui.utils.LocalRevealHostedTabBar
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import kotlinx.coroutines.delay

/*
 * Why: baseline profile journeys (tools/baseline-profile) find the list by its tag. The breakpoint, width cap and
 * spacing are the shared screen's ResponsiveSplitColumn ones.
 */
private const val PLANS_LIST_TAG = "plans_list"
private const val HERO_ITEM_KEY = "hero"
private const val WEEK_SKELETON_COUNT = 4
private const val SCROLL_LAYOUT_SETTLE_MILLIS = 100L
private const val SCROLL_TO_TOP_SETTLE_MILLIS = 400L
private val splitBreakpoint = 600.dp
private val contentMaxWidth = 1000.dp
private val itemSpacing = 8.dp
private val horizontalItemPadding = 16.dp

@Composable
internal fun ReadingPlanContent(
    state: ReadingPlanUiState,
    transitionScopes: ReadingPlanTransitionScopes?,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    ReadingPlanScrollEffect(
        state = state,
        lazyListState = lazyListState,
        onEvent = onEvent,
    )
    ReadingPlanTabScaffold(
        floatingActionButton = {
            ReadingPlanFabsColumn(
                state = state,
                onEvent = onEvent,
            )
        },
    ) { contentPadding ->
        ReadingPlanList(
            state = state,
            transitionScopes = transitionScopes,
            lazyListState = lazyListState,
            contentPadding = contentPadding,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun ReadingPlanScrollEffect(
    state: ReadingPlanUiState,
    lazyListState: LazyListState,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    val activeWeekNumber = (state as? ReadingPlanUiState.Loaded)?.planStatus?.nextDay?.weekNumber
    LaunchedEffect(lazyListState, activeWeekNumber) {
        snapshotFlow { lazyListState.firstVisibleItemIndex to lazyListState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                currentOnEvent(ReadingPlanUiEvent.OnScrollStateChange(isScrolledDown = index > 0 || offset > 0))
                val visibleKeys = lazyListState.layoutInfo.visibleItemsInfo.map { it.key }
                val isActiveRowVisible = HERO_ITEM_KEY in visibleKeys ||
                    (activeWeekNumber != null && activeWeekNumber in visibleKeys)
                currentOnEvent(ReadingPlanUiEvent.OnActiveRowVisibilityChange(isActiveRowVisible))
            }
    }
    val revealHostedTabBar = LocalRevealHostedTabBar.current
    LaunchedEffect(state.scrollToTop) {
        if (state.scrollToTop) {
            revealHostedTabBar()
            lazyListState.animateScrollToItem(index = 0)
            delay(SCROLL_TO_TOP_SETTLE_MILLIS)
            currentOnEvent(ReadingPlanUiEvent.OnScrollToTopCompleted)
        }
    }
}

@Composable
private fun ScrollToWeekEffect(
    state: ReadingPlanUiState,
    includeSidePanel: Boolean,
    lazyListState: LazyListState,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    val currentLoadedState by rememberUpdatedState(state as? ReadingPlanUiState.Loaded)
    val scrollToWeekNumber = state.scrollToWeekNumber
    val isLoaded = state is ReadingPlanUiState.Loaded
    /*
     * Why: keyed on the request, not the whole state. The scroll flips isScrolledDown and isActiveRowVisible, and
     * restarting on those new states would pause and replay the scroll halfway through.
     */
    val revealHostedTabBar = LocalRevealHostedTabBar.current
    LaunchedEffect(scrollToWeekNumber, isLoaded) {
        if (scrollToWeekNumber <= 0 || !isLoaded) return@LaunchedEffect
        val skipAutomaticScroll = state.scrollToWeekIsAutomatic && includeSidePanel
        if (!skipAutomaticScroll) {
            delay(SCROLL_LAYOUT_SETTLE_MILLIS)
            val targetIndex = currentLoadedState?.let { loadedState ->
                getReadingPlanWeekScrollIndex(
                    state = loadedState,
                    includeSidePanel = includeSidePanel,
                    weekNumber = scrollToWeekNumber,
                )
            } ?: -1
            if (targetIndex >= 0) {
                revealHostedTabBar()
                lazyListState.animateScrollToItem(index = targetIndex)
            }
        }
        currentOnEvent(ReadingPlanUiEvent.OnScrollToWeekCompleted)
    }
}

@Composable
private fun ReadingPlanList(
    state: ReadingPlanUiState,
    transitionScopes: ReadingPlanTransitionScopes?,
    lazyListState: LazyListState,
    contentPadding: PaddingValues,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val loadedState = state as? ReadingPlanUiState.Loaded
    val shimmer = if (loadedState == null) rememberShimmer(shimmerBounds = ShimmerBounds.Window) else null
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .testTag(PLANS_LIST_TAG),
    ) {
        val isSplit = maxWidth > splitBreakpoint
        val contentWidth = maxWidth.coerceAtMost(contentMaxWidth)
        ScrollToWeekEffect(
            state = state,
            includeSidePanel = !isSplit,
            lazyListState = lazyListState,
            onEvent = onEvent,
        )
        val sidePanel: LazyListScope.(Dp) -> Unit = { itemWidth ->
            sidePanelItems(
                state = state,
                shimmer = shimmer,
                itemWidth = itemWidth,
                onEvent = onEvent,
            )
        }
        val weeks: LazyListScope.(Dp) -> Unit = { itemWidth ->
            weekItems(
                loadedState = loadedState,
                shimmer = shimmer,
                itemWidth = itemWidth,
                transitionScopes = transitionScopes,
                onEvent = onEvent,
            )
        }
        if (isSplit) {
            ReadingPlanSplitList(
                maxWidth = maxWidth,
                contentWidth = contentWidth,
                contentPadding = contentPadding,
                lazyListState = lazyListState,
                leftContent = sidePanel,
                rightContent = weeks,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(itemSpacing),
            ) {
                sidePanel(contentWidth)
                weeks(contentWidth)
            }
        }
    }
}

/*
 * Why: both columns are full-width lists, so a vertical swipe in the empty margins around the centered content
 * still scrolls, as in the shared ResponsiveSplitColumn.
 */
@Composable
private fun ReadingPlanSplitList(
    maxWidth: Dp,
    contentWidth: Dp,
    contentPadding: PaddingValues,
    lazyListState: LazyListState,
    leftContent: LazyListScope.(Dp) -> Unit,
    rightContent: LazyListScope.(Dp) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val sideMargin = ((maxWidth - contentWidth) / 2).coerceAtLeast(0.dp)
    val halfSpacing = itemSpacing / 2
    val columnWidth = (contentWidth - itemSpacing).coerceAtLeast(0.dp) / 2
    Row(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .width(sideMargin + columnWidth + halfSpacing)
                .fillMaxHeight(),
            state = rememberLazyListState(),
            contentPadding = PaddingValues(
                start = contentPadding.calculateStartPadding(layoutDirection) + sideMargin,
                top = contentPadding.calculateTopPadding(),
                end = halfSpacing,
                bottom = contentPadding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            leftContent(columnWidth)
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            state = lazyListState,
            contentPadding = PaddingValues(
                start = halfSpacing,
                top = contentPadding.calculateTopPadding(),
                end = contentPadding.calculateEndPadding(layoutDirection) + sideMargin,
                bottom = contentPadding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            rightContent(columnWidth)
        }
    }
}

private fun LazyListScope.sidePanelItems(
    state: ReadingPlanUiState,
    shimmer: Shimmer?,
    itemWidth: Dp,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val loadedState = state as? ReadingPlanUiState.Loaded
    val sectionModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = horizontalItemPadding)
    centeredItem(
        key = "header",
        itemWidth = itemWidth,
    ) {
        ReadingPlanHeaderRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = horizontalItemPadding,
                    vertical = 8.dp,
                ),
            selectedReadingPlan = state.selectedReadingPlan,
            isShowingMenu = state.isShowingMenu,
            isShowingOrderMenu = state.isShowingOrderMenu,
            onEvent = onEvent,
        )
    }
    centeredItem(
        key = HERO_ITEM_KEY,
        itemWidth = itemWidth,
    ) {
        if (loadedState != null) {
            ReadingPlanHeroCard(
                modifier = sectionModifier,
                planStatus = loadedState.planStatus,
                onEvent = onEvent,
            )
        } else if (shimmer != null) {
            ReadingPlanHeroSkeleton(
                modifier = sectionModifier,
                shimmer = shimmer,
            )
        }
    }
    centeredItem(
        key = "progress",
        itemWidth = itemWidth,
    ) {
        if (loadedState != null) {
            ReadingPlanProgressCard(
                modifier = sectionModifier,
                state = loadedState,
            )
        } else if (shimmer != null) {
            ReadingPlanProgressSkeleton(
                modifier = sectionModifier,
                shimmer = shimmer,
            )
        }
    }
}

private fun LazyListScope.weekItems(
    loadedState: ReadingPlanUiState.Loaded?,
    shimmer: Shimmer?,
    itemWidth: Dp,
    transitionScopes: ReadingPlanTransitionScopes?,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    if (loadedState == null) {
        if (shimmer != null) {
            weekSkeletonItems(
                shimmer = shimmer,
                itemWidth = itemWidth,
            )
        }
        return
    }
    items(
        items = buildReadingPlanWeekItems(loadedState),
        key = ::getReadingPlanItemKey,
    ) { item ->
        CenteredItemBox(
            modifier = Modifier.animateItem(),
            itemWidth = itemWidth,
        ) {
            when (item) {
                is ReadingPlanListItem.SectionHeader -> ReadingPlanSectionHeaderText(
                    group = item.group,
                    isBehind = loadedState.planStatus.mode == PlanMode.Behind,
                )

                is ReadingPlanListItem.Week -> ReadingPlanWeekCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalItemPadding),
                    week = item.week,
                    flashTargetGlobalIndex = loadedState.flashTargetGlobalIndex,
                    transitionScopes = transitionScopes,
                    onEvent = onEvent,
                )

                is ReadingPlanListItem.ShowMore -> ReadingPlanShowMoreButton(
                    group = item.group,
                    totalWeeks = item.totalWeeks,
                    isExpanded = item.group.isExpanded(loadedState),
                    onEvent = onEvent,
                )
            }
        }
    }
}

private fun LazyListScope.weekSkeletonItems(
    shimmer: Shimmer,
    itemWidth: Dp,
) {
    centeredItem(
        key = "week_shimmer_section_header",
        itemWidth = itemWidth,
    ) {
        ReadingPlanSectionHeaderSkeleton(shimmer = shimmer)
    }
    repeat(WEEK_SKELETON_COUNT) { index ->
        centeredItem(
            key = "week_shimmer_$index",
            itemWidth = itemWidth,
        ) {
            ReadingPlanWeekSkeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalItemPadding),
                shimmer = shimmer,
                isCurrent = index == 0,
            )
        }
    }
}

private fun LazyListScope.centeredItem(
    key: Any,
    itemWidth: Dp,
    content: @Composable () -> Unit,
) {
    item(key = key) {
        CenteredItemBox(
            itemWidth = itemWidth,
            content = content,
        )
    }
}

@Composable
private fun CenteredItemBox(
    itemWidth: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.width(itemWidth)) {
            content()
        }
    }
}

@Composable
private fun ReadingPlanSectionHeaderText(
    group: WeekGroup,
    isBehind: Boolean,
) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 4.dp,
            ),
        text = stringResource(group.toSectionRes(isBehind = isBehind)),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
        color = if (group == WeekGroup.Current) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
}

@Composable
private fun ReadingPlanShowMoreButton(
    group: WeekGroup,
    totalWeeks: Int,
    isExpanded: Boolean,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val hiddenCount = (totalWeeks - COLLAPSED_WEEK_LIMIT).coerceAtLeast(0)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        TextButton(onClick = { onEvent(group.toToggleEvent()) }) {
            Text(
                text = if (isExpanded) {
                    stringResource(R.string.reading_plan_see_less)
                } else {
                    pluralStringResource(R.plurals.reading_plan_see_more_weeks, hiddenCount, hiddenCount)
                },
            )
            Icon(
                modifier = Modifier.padding(start = 6.dp),
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
            )
        }
    }
}

private fun WeekGroup.toSectionRes(isBehind: Boolean): Int = when (this) {
    WeekGroup.Current -> if (isBehind) {
        R.string.reading_plan_section_current_behind
    } else {
        R.string.reading_plan_section_current
    }

    WeekGroup.Upcoming -> R.string.reading_plan_section_upcoming

    WeekGroup.Completed -> R.string.reading_plan_section_completed
}

private fun WeekGroup.isExpanded(state: ReadingPlanUiState.Loaded): Boolean = when (this) {
    WeekGroup.Upcoming -> state.upcomingExpanded
    WeekGroup.Completed -> state.completedExpanded
    WeekGroup.Current -> true
}

private fun WeekGroup.toToggleEvent(): ReadingPlanUiEvent = if (this == WeekGroup.Completed) {
    ReadingPlanUiEvent.OnToggleCompletedExpanded
} else {
    ReadingPlanUiEvent.OnToggleUpcomingExpanded
}
