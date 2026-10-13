package com.quare.bibleplanner.feature.books.presentation.utils

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BookTestamentFilter
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState

@Stable
class BooksLazyGridStates(
    private val searchGridState: LazyGridState,
    private val searchListState: LazyGridState,
    private val allTestamentsGridState: LazyGridState,
    private val allTestamentsListState: LazyGridState,
    private val oldTestamentGridState: LazyGridState,
    private val oldTestamentListState: LazyGridState,
    private val newTestamentGridState: LazyGridState,
    private val newTestamentListState: LazyGridState,
) {
    val states: List<LazyGridState> = listOf(
        searchGridState,
        searchListState,
        allTestamentsGridState,
        allTestamentsListState,
        oldTestamentGridState,
        oldTestamentListState,
        newTestamentGridState,
        newTestamentListState,
    )

    fun getCurrent(state: BooksUiState): LazyGridState {
        if (state !is BooksUiState.Success) return searchGridState
        val isGrid = state.layoutFormat == BookLayoutFormat.Grid
        return if (!state.shouldShowTestamentToggle) {
            if (isGrid) searchGridState else searchListState
        } else {
            when (state.selectedTestamentFilter) {
                BookTestamentFilter.All -> if (isGrid) allTestamentsGridState else allTestamentsListState
                BookTestamentFilter.OldTestament -> if (isGrid) oldTestamentGridState else oldTestamentListState
                BookTestamentFilter.NewTestament -> if (isGrid) newTestamentGridState else newTestamentListState
            }
        }
    }
}

@Composable
fun rememberBooksLazyGridStates(): BooksLazyGridStates {
    val searchGridState = rememberLazyGridState()
    val searchListState = rememberLazyGridState()
    val allTestamentsGridState = rememberLazyGridState()
    val allTestamentsListState = rememberLazyGridState()
    val oldTestamentGridState = rememberLazyGridState()
    val oldTestamentListState = rememberLazyGridState()
    val newTestamentGridState = rememberLazyGridState()
    val newTestamentListState = rememberLazyGridState()
    return remember {
        BooksLazyGridStates(
            searchGridState = searchGridState,
            searchListState = searchListState,
            allTestamentsGridState = allTestamentsGridState,
            allTestamentsListState = allTestamentsListState,
            oldTestamentGridState = oldTestamentGridState,
            oldTestamentListState = oldTestamentListState,
            newTestamentGridState = newTestamentGridState,
            newTestamentListState = newTestamentListState,
        )
    }
}
