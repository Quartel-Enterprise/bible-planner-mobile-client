package com.quare.bibleplanner.feature.read.presentation.screen

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

private const val PREFETCH_ITEM_COUNT = 6

@Composable
internal fun ReachedEndEffect(
    listState: LazyListState,
    chapters: List<ReadChapterUiModel>,
    onReachedEnd: () -> Unit,
) {
    LaunchedEffect(listState, chapters) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index
        }.filterNotNull()
            .map { lastVisibleIndex -> lastVisibleIndex >= listState.layoutInfo.totalItemsCount - PREFETCH_ITEM_COUNT }
            .distinctUntilChanged()
            .filter { isNearEnd -> isNearEnd }
            .collect { onReachedEnd() }
    }
}
