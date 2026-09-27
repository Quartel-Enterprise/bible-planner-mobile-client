package com.quare.bibleplanner.feature.verse.annotations.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.verse.annotations.generated.resources.Res
import bibleplanner.feature.verse.annotations.generated.resources.annotations_title
import bibleplanner.feature.verse.annotations.generated.resources.item_count
import bibleplanner.feature.verse.annotations.generated.resources.search
import bibleplanner.feature.verse.annotations.generated.resources.search_hint
import bibleplanner.feature.verse.annotations.generated.resources.search_hint_short
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import com.quare.bibleplanner.ui.icons.AppIcon
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val wideSearchFieldWidth = 300.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AnnotationsTopBar(
    platform: Platform,
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
                    placeholder = stringResource(Res.string.search_hint),
                    isCollapsible = true,
                    platform = platform,
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
        title = { Text(text = stringResource(Res.string.annotations_title)) },
        navigationIcon = {
            BackIcon(
                platform = platform,
                onBackClick = { onEvent(AnnotationsUiEvent.OnBackClick) },
            )
        },
        actions = {
            shownCount?.takeIf { it > 0 }?.let { count ->
                Text(
                    text = pluralStringResource(
                        Res.plurals.item_count,
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
                        placeholder = stringResource(Res.string.search_hint_short),
                        isCollapsible = false,
                        platform = platform,
                        onEvent = onEvent,
                        modifier = Modifier
                            .width(wideSearchFieldWidth)
                            .padding(end = 16.dp),
                    )
                } else {
                    CommonIconButton(
                        icon = AppIcon.Search,
                        contentDescription = stringResource(Res.string.search),
                        onClick = { onEvent(AnnotationsUiEvent.OnSearchClick) },
                    )
                }
            }
        },
    )
}
