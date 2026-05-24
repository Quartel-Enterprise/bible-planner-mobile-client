package com.quare.bibleplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Variant of [ResponsiveColumn] that renders the landscape layout as TWO independent
 * scrollable columns (left panel + right list).
 *
 * Both columns are [LazyColumn]s that fill the full screen width split by [leftWeight] /
 * [rightWeight], so vertical scroll keeps working in the empty side margins around the
 * centered content (each margin scrolls the column on its side).
 *
 * In portrait (width <= [maxPortraitWidth]) it falls back to the same behavior as
 * [ResponsiveColumn]: a single LazyColumn filling the screen with items centered at
 * [maxContentWidth].
 *
 * @param lazyListState state for the main scrollable list. Used in portrait, and for the
 * RIGHT column in landscape — typically where the long, dynamic content lives.
 * @param leftLazyListState state for the LEFT column in landscape only.
 */
@Composable
fun ResponsiveSplitColumn(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    leftLazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    horizontalSpacing: Dp = 0.dp,
    leftWeight: Float = 0.5f,
    rightWeight: Float = 0.5f,
    maxPortraitWidth: Dp = 600.dp,
    maxContentWidth: Dp = 800.dp,
    portraitContent: ResponsiveContentScope.() -> Unit,
    landscapeLeftContent: ResponsiveContentScope.() -> Unit,
    landscapeRightContent: ResponsiveContentScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = this.maxWidth > maxPortraitWidth
        val contentMaxWidth = this.maxWidth.coerceAtMost(maxContentWidth)

        if (isLandscape) {
            val layoutDirection = LocalLayoutDirection.current
            val sideMargin = ((this.maxWidth - contentMaxWidth) / 2).coerceAtLeast(0.dp)
            val halfSpacing = horizontalSpacing / 2
            val availableForContent = (contentMaxWidth - horizontalSpacing).coerceAtLeast(0.dp)
            val leftContentWidth = availableForContent * leftWeight
            val rightContentWidth = availableForContent * rightWeight
            val leftAreaWidth = sideMargin + leftContentWidth + halfSpacing

            val leftPadding = PaddingValues(
                start = contentPadding.calculateStartPadding(layoutDirection) + sideMargin,
                top = contentPadding.calculateTopPadding(),
                end = halfSpacing,
                bottom = contentPadding.calculateBottomPadding(),
            )
            val rightPadding = PaddingValues(
                start = halfSpacing,
                top = contentPadding.calculateTopPadding(),
                end = contentPadding.calculateEndPadding(layoutDirection) + sideMargin,
                bottom = contentPadding.calculateBottomPadding(),
            )

            Row(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .width(leftAreaWidth)
                        .fillMaxHeight(),
                    state = leftLazyListState,
                    contentPadding = leftPadding,
                    verticalArrangement = verticalArrangement,
                ) {
                    val scope = ResponsiveContentScopeImpl(
                        lazyListScope = this,
                        contentMaxWidth = leftContentWidth,
                    )
                    landscapeLeftContent(scope)
                }
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    state = lazyListState,
                    contentPadding = rightPadding,
                    verticalArrangement = verticalArrangement,
                ) {
                    val scope = ResponsiveContentScopeImpl(
                        lazyListScope = this,
                        contentMaxWidth = rightContentWidth,
                    )
                    landscapeRightContent(scope)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                contentPadding = contentPadding,
                verticalArrangement = verticalArrangement,
            ) {
                val scope = ResponsiveContentScopeImpl(
                    lazyListScope = this,
                    contentMaxWidth = contentMaxWidth,
                )
                portraitContent(scope)
            }
        }
    }
}
