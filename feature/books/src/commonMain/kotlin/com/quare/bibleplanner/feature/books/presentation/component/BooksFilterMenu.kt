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
import com.quare.bibleplanner.feature.books.presentation.model.BookFilterOption
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BooksFilterMenu(
    isVisible: Boolean,
    filterOptions: List<BookFilterOption>,
    onEvent: (BooksUiEvent) -> Unit,
) {
    DropdownMenu(
        expanded = isVisible,
        onDismissRequest = { onEvent(BooksUiEvent.OnDismissFilterMenu) },
    ) {
        filterOptions.forEach { option ->
            DropdownMenuItem(
                text = { Text(stringResource(option.label)) },
                onClick = { onEvent(BooksUiEvent.OnToggleFilter(option.type)) },
                trailingIcon = if (option.isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
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
