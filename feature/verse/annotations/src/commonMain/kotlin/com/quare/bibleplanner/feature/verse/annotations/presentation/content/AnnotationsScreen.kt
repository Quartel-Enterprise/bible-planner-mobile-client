package com.quare.bibleplanner.feature.verse.annotations.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.verse.annotations.presentation.component.AnnotationsTopBar
import com.quare.bibleplanner.feature.verse.annotations.presentation.component.CustomRangePickerDialog
import com.quare.bibleplanner.feature.verse.annotations.presentation.component.NarrowFilterBar
import com.quare.bibleplanner.feature.verse.annotations.presentation.component.RemoveAnnotationDialog
import com.quare.bibleplanner.feature.verse.annotations.presentation.component.WideFilterPanel
import com.quare.bibleplanner.feature.verse.annotations.presentation.content.AnnotationsEmptyContent
import com.quare.bibleplanner.feature.verse.annotations.presentation.content.AnnotationsList
import com.quare.bibleplanner.feature.verse.annotations.presentation.content.AnnotationsLoadingContent
import com.quare.bibleplanner.feature.verse.annotations.presentation.content.AnnotationsNoResultsContent
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsContentUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiState

private val wideLayoutMinWidth = 840.dp
private val filterPanelWidth = 220.dp
private val listMaxWidth = 680.dp

@Composable
internal fun AnnotationsScreen(
    platform: Platform,
    state: AnnotationsUiState,
    onEvent: (AnnotationsUiEvent) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= wideLayoutMinWidth
        val loaded = state.content.valueOrNull()
        Scaffold(
            topBar = {
                AnnotationsTopBar(
                    platform = platform,
                    shownCount = loaded?.shownCount,
                    isSearchAvailable = loaded != null && loaded.totalCount > 0,
                    isSearchOpen = state.isSearchOpen,
                    searchQuery = state.searchQuery,
                    isWide = isWide,
                    onEvent = onEvent,
                )
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            ) {
                when {
                    loaded == null -> AnnotationsLoadingContent()

                    loaded.totalCount == 0 -> AnnotationsEmptyContent()

                    isWide -> WideAnnotationsContent(
                        content = loaded,
                        state = state,
                        onEvent = onEvent,
                    )

                    else -> NarrowAnnotationsContent(
                        content = loaded,
                        state = state,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
    val loadedContent = state.content.valueOrNull()
    if (state.isCustomRangePickerOpen && loadedContent != null) {
        CustomRangePickerDialog(
            customRange = loadedContent.customRange,
            today = loadedContent.today,
            onEvent = onEvent,
        )
    }
    state.pendingRemoval?.let { item ->
        RemoveAnnotationDialog(
            item = item,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun NarrowAnnotationsContent(
    content: AnnotationsContentUiModel,
    state: AnnotationsUiState,
    onEvent: (AnnotationsUiEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        NarrowFilterBar(
            content = content,
            openFilterMenu = state.openFilterMenu,
            onEvent = onEvent,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        AnnotationsResultsContent(
            content = content,
            state = state,
            isWide = false,
            onEvent = onEvent,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp,
            ),
        )
    }
}

@Composable
private fun WideAnnotationsContent(
    content: AnnotationsContentUiModel,
    state: AnnotationsUiState,
    onEvent: (AnnotationsUiEvent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        WideFilterPanel(
            content = content,
            onEvent = onEvent,
            modifier = Modifier
                .width(filterPanelWidth)
                .padding(top = 8.dp),
        )
        AnnotationsResultsContent(
            content = content,
            state = state,
            isWide = true,
            onEvent = onEvent,
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = 32.dp,
            ),
            modifier = Modifier.widthIn(max = listMaxWidth),
        )
    }
}

@Composable
private fun AnnotationsResultsContent(
    content: AnnotationsContentUiModel,
    state: AnnotationsUiState,
    isWide: Boolean,
    onEvent: (AnnotationsUiEvent) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    if (content.shownCount == 0) {
        AnnotationsNoResultsContent(
            query = state.searchQuery.trim(),
            onClearFiltersClick = { onEvent(AnnotationsUiEvent.OnClearFiltersClick) },
            modifier = modifier,
        )
    } else {
        AnnotationsList(
            groups = content.groups,
            openMenuItemKey = state.openMenuItemKey,
            isWide = isWide,
            onEvent = onEvent,
            contentPadding = contentPadding,
            modifier = modifier.fillMaxSize(),
        )
    }
}
