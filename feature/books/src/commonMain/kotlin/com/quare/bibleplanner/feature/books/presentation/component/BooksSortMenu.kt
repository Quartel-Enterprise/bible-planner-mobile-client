package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.sort_alphabetical_ascending
import bibleplanner.feature.books.generated.resources.sort_alphabetical_descending
import com.quare.bibleplanner.feature.books.presentation.model.BookSortOrder
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.ui.component.AppDropdownMenu
import com.quare.bibleplanner.ui.component.AppDropdownMenuItem
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BoxScope.BooksSortMenu(
    isVisible: Boolean,
    currentOrder: BookSortOrder?,
    onEvent: (BooksUiEvent) -> Unit,
) {
    AppDropdownMenu(
        isExpanded = isVisible,
        onDismissRequest = { onEvent(BooksUiEvent.OnDismissSortMenu) },
        items = BookSortOrder.entries.map { order ->
            AppDropdownMenuItem(
                title = stringResource(order.toLabelResource()),
                icon = null,
                isSelected = currentOrder == order,
                isDestructive = false,
                onClick = { onEvent(BooksUiEvent.OnSortOrderSelect(order)) },
            )
        },
    )
}

private fun BookSortOrder.toLabelResource(): StringResource = when (this) {
    BookSortOrder.AlphabeticalAscending -> Res.string.sort_alphabetical_ascending
    BookSortOrder.AlphabeticalDescending -> Res.string.sort_alphabetical_descending
}
