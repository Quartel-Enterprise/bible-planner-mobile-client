package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.runtime.Composable
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.all_testaments
import com.quare.bibleplanner.feature.books.presentation.model.BookTestamentFilter
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.ui.component.AppDropdownChip
import com.quare.bibleplanner.ui.component.AppDropdownMenuItem
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TestamentFilterChip(
    selectedTestamentFilter: BookTestamentFilter,
    isMenuVisible: Boolean,
    onEvent: (BooksUiEvent) -> Unit,
) {
    AppDropdownChip(
        label = stringResource(selectedTestamentFilter.toLabelResource()),
        isExpanded = isMenuVisible,
        menuItems = BookTestamentFilter.entries.map { testamentFilter ->
            AppDropdownMenuItem(
                title = stringResource(testamentFilter.toLabelResource()),
                icon = null,
                isSelected = testamentFilter == selectedTestamentFilter,
                isDestructive = false,
                onClick = { onEvent(BooksUiEvent.OnTestamentSelect(testamentFilter)) },
            )
        },
        onClick = { onEvent(BooksUiEvent.OnToggleTestamentMenu) },
        onDismissRequest = { onEvent(BooksUiEvent.OnDismissTestamentMenu) },
    )
}

private fun BookTestamentFilter.toLabelResource(): StringResource = testament?.titleRes ?: Res.string.all_testaments
