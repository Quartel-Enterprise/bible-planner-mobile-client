package com.quare.bibleplanner.android.feature.books

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.android.navigation.LocalMainEntryAnimatedContentScope
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiAction
import com.quare.bibleplanner.feature.books.presentation.utils.rememberBooksLazyGridStates
import com.quare.bibleplanner.feature.books.presentation.viewmodel.BooksViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.LocalHostedTabBarHeight
import com.quare.bibleplanner.ui.utils.LocalRevealHostedTabBar
import com.quare.bibleplanner.ui.utils.ReserveHostedTabBarHeightEffect
import com.quare.bibleplanner.ui.utils.transition.LocalSharedTransitionScope
import org.koin.compose.viewmodel.koinViewModel

internal fun MainNavRouteDestination.Books.toNavEntry(): NavEntry<NavKey> = NavEntry(key = this) {
    BooksTabEntryContent()
}

@Composable
private fun BooksTabEntryContent() {
    val viewModel = koinViewModel<BooksViewModel>()
    val state by viewModel.uiState.collectAsState()
    val lazyGridStates = rememberBooksLazyGridStates()
    val isScrolled by remember(state) {
        derivedStateOf {
            val activeState = lazyGridStates.getCurrent(state)
            activeState.firstVisibleItemIndex > 0 || activeState.firstVisibleItemScrollOffset > 0
        }
    }
    val revealHostedTabBar = LocalRevealHostedTabBar.current
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            BooksUiAction.ScrollToTop -> {
                revealHostedTabBar()
                lazyGridStates.scrollAllToTop()
            }
        }
    }
    ReserveHostedTabBarHeightEffect()
    val transitionScopes = rememberBooksTransitionScopes()
    ExpressiveTheme {
        BooksContent(
            state = state,
            isScrolled = isScrolled,
            lazyGridState = lazyGridStates.getCurrent(state),
            transitionScopes = transitionScopes,
            hostedTabBarHeight = LocalHostedTabBarHeight.current ?: 0.dp,
            onEvent = viewModel::onEvent,
        )
    }
}

@Composable
private fun rememberBooksTransitionScopes(): BooksTransitionScopes? {
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return null
    val animatedVisibilityScope = LocalMainEntryAnimatedContentScope.current ?: return null
    // Why: a new instance on every state change would keep every book item from skipping recomposition.
    return remember(sharedTransitionScope, animatedVisibilityScope) {
        BooksTransitionScopes(
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }
}
