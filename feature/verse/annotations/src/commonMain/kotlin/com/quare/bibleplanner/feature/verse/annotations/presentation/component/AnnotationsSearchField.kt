package com.quare.bibleplanner.feature.verse.annotations.presentation.component

import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextOverflow
import bibleplanner.feature.verse.annotations.generated.resources.Res
import bibleplanner.feature.verse.annotations.generated.resources.clear_search
import bibleplanner.feature.verse.annotations.generated.resources.search
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import com.quare.bibleplanner.ui.icons.AppIcon
import com.quare.bibleplanner.ui.icons.Icon
import org.jetbrains.compose.resources.stringResource

private const val PLACEHOLDER_ALPHA = 0.6f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AnnotationsSearchField(
    query: String,
    placeholder: String,
    isCollapsible: Boolean,
    platform: Platform,
    onEvent: (AnnotationsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    if (isCollapsible) {
        LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    }
    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = { onEvent(AnnotationsUiEvent.OnSearchQueryChange(it)) },
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier.focusRequester(focusRequester),
                placeholder = {
                    Text(
                        text = placeholder,
                        modifier = Modifier.alpha(PLACEHOLDER_ALPHA),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                leadingIcon = {
                    if (isCollapsible) {
                        BackIcon(
                            platform = platform,
                            onBackClick = { onEvent(AnnotationsUiEvent.OnSearchCloseClick) },
                        )
                    } else {
                        Icon(
                            icon = AppIcon.Search,
                            contentDescription = stringResource(Res.string.search),
                        )
                    }
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        CommonIconButton(
                            icon = AppIcon.Close,
                            contentDescription = stringResource(Res.string.clear_search),
                            onClick = { onEvent(AnnotationsUiEvent.OnSearchClearClick) },
                        )
                    }
                },
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier = modifier,
        shape = SearchBarDefaults.dockedShape,
    ) {}
}
