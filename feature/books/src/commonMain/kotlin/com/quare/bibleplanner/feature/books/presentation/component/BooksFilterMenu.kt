package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.favorites
import bibleplanner.feature.books.generated.resources.read
import bibleplanner.feature.books.generated.resources.unread
import com.quare.bibleplanner.feature.books.presentation.model.BookFilterOption
import com.quare.bibleplanner.feature.books.presentation.model.BookFilterType
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.ui.component.AppDropdownMenu
import com.quare.bibleplanner.ui.component.AppDropdownMenuItem
import org.jetbrains.compose.resources.StringResource
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
                title = stringResource(option.type.toLabel()),
                icon = null,
                isSelected = option.isSelected,
                isDestructive = false,
                onClick = { onEvent(BooksUiEvent.OnToggleFilter(option.type)) },
            )
        },
    )
}

private fun BookFilterType.toLabel(): StringResource = when (this) {
    BookFilterType.OnlyRead -> Res.string.read
    BookFilterType.OnlyUnread -> Res.string.unread
    BookFilterType.Favorites -> Res.string.favorites
}
