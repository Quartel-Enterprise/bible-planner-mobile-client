package com.quare.bibleplanner.android.feature.readingplan

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.android.navigation.LocalMainAnimatedContentScope
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiAction
import com.quare.bibleplanner.feature.readingplan.presentation.viewmodel.ReadingPlanViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import com.quare.bibleplanner.ui.utils.transition.LocalSharedTransitionScope
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

internal fun MainNavRouteDestination.Plans.toNavEntry(): NavEntry<NavKey> = NavEntry(key = this) {
    ReadingPlanEntryContent()
}

@Composable
private fun ReadingPlanEntryContent() {
    val viewModel = koinViewModel<ReadingPlanViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val appSnackbarController = koinInject<AppSnackbarController>()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    ActionCollector(viewModel.uiAction) { uiAction ->
        when (uiAction) {
            is ReadingPlanUiAction.OpenLink -> uriHandler.openUri(uiAction.url)
            ReadingPlanUiAction.ShowNoProgressToDelete -> appSnackbarController.show(context.createNoProgressMessage())
        }
    }
    ExpressiveTheme {
        ReadingPlanContent(
            state = uiState,
            transitionScopes = rememberReadingPlanTransitionScopes(),
            onEvent = viewModel::onEvent,
        )
    }
}

@Composable
private fun rememberReadingPlanTransitionScopes(): ReadingPlanTransitionScopes? {
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return null
    val animatedVisibilityScope = LocalMainAnimatedContentScope.current ?: return null
    // Why: a new instance on every state change would keep every day item from skipping recomposition.
    return remember(sharedTransitionScope, animatedVisibilityScope) {
        ReadingPlanTransitionScopes(
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }
}

private fun Context.createNoProgressMessage(): AppSnackbarMessage = AppSnackbarMessage.Text(
    text = getString(R.string.reading_plan_no_progress_to_delete_message),
    isDismissible = false,
)
