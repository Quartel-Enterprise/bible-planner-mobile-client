package com.quare.bibleplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ResponsiveContent(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    maxPortraitWidth: Dp = 600.dp,
    maxContentWidth: Dp = 1200.dp,
    portraitContent: LazyListScope.(contentMaxWidth: Dp) -> Unit,
    landscapeContent: (LazyListScope.(contentMaxWidth: Dp) -> Unit)? = null,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = landscapeContent != null && this.maxWidth > maxPortraitWidth
        val contentMaxWidth = this.maxWidth.coerceAtMost(maxContentWidth)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
        ) {
            if (isLandscape) {
                landscapeContent(contentMaxWidth)
            } else {
                portraitContent(contentMaxWidth)
            }
        }
    }
}

/**
 * Helper to wrap an item in a centered box with a max width.
 */
fun LazyListScope.centeredItem(
    contentMaxWidth: Dp,
    content: @Composable () -> Unit
) {
    item {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.width(contentMaxWidth)) {
                content()
            }
        }
    }
}
