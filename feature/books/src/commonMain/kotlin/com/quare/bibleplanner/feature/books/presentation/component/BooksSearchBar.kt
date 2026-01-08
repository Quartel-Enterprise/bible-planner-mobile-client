package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.content_description_clear_search
import bibleplanner.feature.books.generated.resources.content_description_search
import bibleplanner.feature.books.generated.resources.search_books
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BooksSearchBar(
    query: String,
    onEvent: (BooksUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = SearchBarDefaults.dockedShape,
) {
    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = { onEvent(BooksUiEvent.OnSearchQueryChange(it)) },
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                placeholder = { Text(stringResource(Res.string.search_books)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(Res.string.content_description_search),
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        CommonIconButton(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(
                                Res.string.content_description_clear_search,
                            ),
                            onClick = { onEvent(BooksUiEvent.OnSearchQueryChange("")) },
                        )
                    }
                },
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier = modifier,
        shape = shape,
    ) {}
}
