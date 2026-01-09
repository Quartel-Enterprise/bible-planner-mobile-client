package com.quare.bibleplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ResponsiveColumn(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    maxPortraitWidth: Dp = 600.dp,
    maxContentWidth: Dp = 1200.dp,
    portraitContent: ResponsiveContentScope.() -> Unit,
    landscapeContent: (ResponsiveContentScope.() -> Unit)? = null,
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
            val scope = ResponsiveContentScopeImpl(
                lazyListScope = this,
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
