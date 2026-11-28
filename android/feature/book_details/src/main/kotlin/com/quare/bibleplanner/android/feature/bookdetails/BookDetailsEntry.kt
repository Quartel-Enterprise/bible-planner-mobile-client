package com.quare.bibleplanner.android.feature.bookdetails

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.BookDetailsNavRoute
import com.quare.bibleplanner.feature.bookdetails.presentation.viewmodel.BookDetailsViewModel
import com.quare.bibleplanner.ui.utils.transition.LocalSharedTransitionScope
import com.quare.bibleplanner.ui.utils.transition.TransitionScopes
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun BookDetailsNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(key = this) {
    val viewModel = koinViewModel<BookDetailsViewModel> { parametersOf(this) }
    val uiState by viewModel.uiState.collectAsState()
    val transitionScopes = LocalSharedTransitionScope.current?.let { sharedTransitionScope ->
        val animatedVisibilityScope = LocalNavAnimatedContentScope.current
        // Why: a new instance on every state change would keep every chapter item from skipping recomposition.
        remember(sharedTransitionScope, animatedVisibilityScope) {
            TransitionScopes(
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    }
    ExpressiveTheme {
        BookDetailsContent(
            state = uiState,
            transitionScopes = transitionScopes,
            onEvent = viewModel::onEvent,
        )
    }
}
