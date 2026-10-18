package com.quare.bibleplanner.feature.books.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun BooksUiActionCollector(
    uiAction: Flow<BooksUiAction>,
    lazyGridStates: BooksLazyGridStates,
) {
    ActionCollector(uiAction) { action ->
        when (action) {
            is BooksUiAction.ScrollToTop -> lazyGridStates.scrollAllToTop()
        }
    }
}
