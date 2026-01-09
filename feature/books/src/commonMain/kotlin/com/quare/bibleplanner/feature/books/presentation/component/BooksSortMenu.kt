package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.content_description_selected
import bibleplanner.feature.books.generated.resources.sort_alphabetical_ascending
import bibleplanner.feature.books.generated.resources.sort_alphabetical_descending
import com.quare.bibleplanner.feature.books.presentation.model.BookSortOrder
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BooksSortMenu(
    isVisible: Boolean,
    currentOrder: BookSortOrder?,
    onEvent: (BooksUiEvent) -> Unit,
) {
    DropdownMenu(
        expanded = isVisible,
        onDismissRequest = { onEvent(BooksUiEvent.OnDismissSortMenu) },
    ) {
        BookSortOrder.values().forEach { order ->
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(
                            when (order) {
                                BookSortOrder.AlphabeticalAscending -> Res.string.sort_alphabetical_ascending
                                BookSortOrder.AlphabeticalDescending -> Res.string.sort_alphabetical_descending
                            },
                        ),
                    )
                },
                onClick = { onEvent(BooksUiEvent.OnSortOrderSelect(order)) },
                trailingIcon = if (currentOrder == order) {
                    {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(
                                Res.string.content_description_selected,
                            ),
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}
