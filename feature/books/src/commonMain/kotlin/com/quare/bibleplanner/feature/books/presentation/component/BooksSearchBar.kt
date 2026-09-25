package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.content_description_clear_search
import bibleplanner.feature.books.generated.resources.content_description_search
import bibleplanner.feature.books.generated.resources.search_books
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import com.quare.bibleplanner.ui.icons.AppIcon
import com.quare.bibleplanner.ui.icons.Icon
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BooksSearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onEvent: (BooksUiEvent) -> Unit,
    shape: Shape = SearchBarDefaults.dockedShape,
) {
    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = { onEvent(BooksUiEvent.OnSearchQueryChange(it)) },
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                placeholder = {
                    Text(
                        text = stringResource(Res.string.search_books),
                        modifier = Modifier.alpha(0.6f).fillMaxWidth(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                leadingIcon = {
                    Icon(
                        icon = AppIcon.Search,
                        contentDescription = stringResource(Res.string.content_description_search),
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        CommonIconButton(
                            icon = AppIcon.Close,
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
