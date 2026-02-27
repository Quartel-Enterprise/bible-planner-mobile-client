package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent

@Composable
internal fun NavigationSuggestionComponent(
    modifier: Modifier = Modifier,
    navigationSuggestions: ReadNavigationSuggestionsModel,
    onEvent: (ReadUiEvent) -> Unit,
    centerComponent: @Composable BoxScope.() -> Unit = {},
) {
    val (previous, next) = navigationSuggestions.run { previous to next }
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        previous?.let { safePrevious ->
            NavigationSuggestionButton(
                modifier = Modifier.align(Alignment.CenterStart),
                suggestion = safePrevious,
                isNext = false,
                onClick = { onEvent(ReadUiEvent.OnNavigationSuggestionClick(safePrevious)) },
            )
        }
        centerComponent()
        next?.let { safeNext ->
            NavigationSuggestionButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                suggestion = safeNext,
                isNext = true,
                onClick = { onEvent(ReadUiEvent.OnNavigationSuggestionClick(safeNext)) },
            )
        }
    }
}
