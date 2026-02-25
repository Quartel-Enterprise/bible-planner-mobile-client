package com.quare.bibleplanner.feature.bookdetails.presentation.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiEvent
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiState
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.component.icon.FavoriteIconButton
import com.quare.bibleplanner.ui.utils.SharedTransitionModifierFactory
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun BookDetailsTopBar(
    state: BookDetailsUiState,
    isScrolled: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onEvent: (BookDetailsUiEvent) -> Unit,
) {
    Surface(
        shadowElevation = if (isScrolled) 4.dp else 0.dp,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        CenterAlignedTopAppBar(
            title = {
                if (state is BookDetailsUiState.Success) {
                    val bookName = stringResource(state.nameStringResource)
                    Text(
                        text = bookName,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = SharedTransitionModifierFactory.getBookNameSharedTransitionModifier(
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope,
                            bookName = bookName,
                        ),
                    )
                }
            },
            navigationIcon = {
                BackIcon(
                    onBackClick = { onEvent(BookDetailsUiEvent.OnBackClick) },
                )
            },
            actions = {
                if (state is BookDetailsUiState.Success) {
                    FavoriteIconButton(
                        isFavorite = state.isFavorite,
                        onClick = { onEvent(BookDetailsUiEvent.OnToggleFavorite) },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        sharedElementKey = "favorite-${state.id.name}",
                        unselectedTint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
        )
    }
}
