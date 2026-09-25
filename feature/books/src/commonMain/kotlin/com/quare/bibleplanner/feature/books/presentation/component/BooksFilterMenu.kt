package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.books.presentation.model.BookFilterOption
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.ui.component.AppDropdownMenu
import com.quare.bibleplanner.ui.component.AppDropdownMenuItem
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BoxScope.BooksFilterMenu(
    isVisible: Boolean,
    filterOptions: List<BookFilterOption>,
    onEvent: (BooksUiEvent) -> Unit,
) {
    AppDropdownMenu(
        isExpanded = isVisible,
        onDismissRequest = { onEvent(BooksUiEvent.OnDismissFilterMenu) },
        items = filterOptions.map { option ->
            AppDropdownMenuItem(
                title = stringResource(option.label),
                icon = null,
                isSelected = option.isSelected,
                isDestructive = false,
                onClick = { onEvent(BooksUiEvent.OnToggleFilter(option.type)) },
            )
        },
    )
}
