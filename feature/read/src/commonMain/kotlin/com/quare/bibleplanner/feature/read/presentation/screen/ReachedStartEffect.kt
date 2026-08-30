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
internal fun ReachedStartEffect(
    listState: LazyListState,
    chapters: List<ReadChapterUiModel>,
    onReachedStart: () -> Unit,
) {
    LaunchedEffect(listState, chapters) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo
                .firstOrNull()
                ?.index
        }.filterNotNull()
            .map { firstVisibleIndex -> firstVisibleIndex <= PREFETCH_ITEM_COUNT }
            .distinctUntilChanged()
            .filter { isNearStart -> isNearStart }
            .collect { onReachedStart() }
    }
}
