package com.quare.bibleplanner.android.feature.annotations

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.text.rememberSyncedTextFieldState
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent

private val wideSearchFieldWidth = 300.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AnnotationsTopBar(
    shownCount: Int?,
    isSearchAvailable: Boolean,
    isSearchOpen: Boolean,
    searchQuery: String,
    isWide: Boolean,
    onEvent: (AnnotationsUiEvent) -> Unit,
) {
    if (isSearchOpen && !isWide) {
        TopAppBar(
            title = {
                AnnotationsSearchField(
                    query = searchQuery,
                    placeholder = stringResource(R.string.annotations_search_hint),
                    isCollapsible = true,
                    onEvent = onEvent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 12.dp),
                )
            },
        )
        return
    }
    TopAppBar(
        title = { Text(text = stringResource(R.string.annotations_title)) },
        navigationIcon = {
            AnnotationsBackButton(onClick = { onEvent(AnnotationsUiEvent.OnBackClick) })
        },
        actions = {
            shownCount?.takeIf { it > 0 }?.let { count ->
                Text(
                    text = pluralStringResource(
                        R.plurals.annotations_item_count,
                        count,
                        count,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
            if (isSearchAvailable) {
                if (isWide) {
                    AnnotationsSearchField(
                        query = searchQuery,
                        placeholder = stringResource(R.string.annotations_search_hint_short),
                        isCollapsible = false,
                        onEvent = onEvent,
                        modifier = Modifier
                            .width(wideSearchFieldWidth)
                            .padding(end = 16.dp),
                    )
                } else {
                    IconButton(onClick = { onEvent(AnnotationsUiEvent.OnSearchClick) }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.annotations_search),
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun AnnotationsBackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.annotations_back),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnnotationsSearchField(
    query: String,
    placeholder: String,
    isCollapsible: Boolean,
    onEvent: (AnnotationsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    if (isCollapsible) {
        LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    }
    val synced = rememberSyncedTextFieldState(
        value = query,
        onValueChange = { onEvent(AnnotationsUiEvent.OnSearchQueryChange(it)) },
    )
    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                state = synced.textFieldState,
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier.focusRequester(focusRequester),
                placeholder = {
                    Text(
                        text = placeholder,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                leadingIcon = {
                    if (isCollapsible) {
                        AnnotationsBackButton(onClick = { onEvent(AnnotationsUiEvent.OnSearchCloseClick) })
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                        )
                    }
                },
                trailingIcon = {
                    if (synced.textFieldState.text.isNotEmpty()) {
                        IconButton(onClick = { onEvent(AnnotationsUiEvent.OnSearchClearClick) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.annotations_clear_search),
                            )
                        }
                    }
                },
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier = modifier,
    ) {}
}
