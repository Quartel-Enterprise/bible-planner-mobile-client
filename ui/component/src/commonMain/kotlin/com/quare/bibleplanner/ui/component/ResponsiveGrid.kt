package com.quare.bibleplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ResponsiveGrid(
    columns: GridCells,
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(0.dp),
    maxPortraitWidth: Dp = 600.dp,
    maxContentWidth: Dp = 800.dp,
    portraitContent: ResponsiveGridScope.() -> Unit,
    landscapeContent: (ResponsiveGridScope.() -> Unit)? = null,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = landscapeContent != null && this.maxWidth > maxPortraitWidth
        val contentMaxWidth = this.maxWidth.coerceAtMost(maxContentWidth)
        val horizontalPadding = ((this.maxWidth - contentMaxWidth) / 2).coerceAtLeast(0.dp)

        val layoutDirection = LocalLayoutDirection.current
        val mergedPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(layoutDirection) + horizontalPadding,
            top = contentPadding.calculateTopPadding(),
            end = contentPadding.calculateEndPadding(layoutDirection) + horizontalPadding,
            bottom = contentPadding.calculateBottomPadding(),
        )

        LazyVerticalGrid(
            columns = columns,
            modifier = Modifier.fillMaxSize(),
            state = lazyGridState,
            contentPadding = mergedPadding,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement,
        ) {
            val scope = ResponsiveGridScopeImpl(
                lazyGridScope = this,
                contentMaxWidth = contentMaxWidth,
            )
            if (isLandscape) {
                landscapeContent(scope)
            } else {
                portraitContent(scope)
            }
        }
    }
}
