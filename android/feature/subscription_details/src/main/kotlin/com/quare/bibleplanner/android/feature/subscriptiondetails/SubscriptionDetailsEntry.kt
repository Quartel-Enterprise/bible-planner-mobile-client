package com.quare.bibleplanner.android.feature.subscriptiondetails

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.android.designsystem.dialog.HideSceneDialogScrimEffect
import com.quare.bibleplanner.android.designsystem.dialog.rememberAlertDialogResolution
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.SubscriptionDetailsNavRoute
import com.quare.bibleplanner.feature.subscriptiondetails.presentation.model.SubscriptionDetailsUiState
import com.quare.bibleplanner.feature.subscriptiondetails.presentation.viewmodel.SubscriptionDetailsViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun SubscriptionDetailsNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = DialogSceneStrategy.dialog(),
) {
    val viewModel = koinViewModel<SubscriptionDetailsViewModel>()
    val navigator = koinInject<Navigator>()
    val uiState by viewModel.uiState.collectAsState()
    // Why: the pop lands asynchronously, so Close and the Error pop must not both pop, or the profile goes too.
    val closeGuard = rememberAlertDialogResolution()
    // Why: the scene window shows while the subscription loads, and its scrim must not flash before the dialog's.
    HideSceneDialogScrimEffect()
    when (val state = uiState) {
        SubscriptionDetailsUiState.Loading -> Unit

        /*
         * Why: without an active subscription there is nothing to show, and an empty dialog window would block
         * the screen.
         */
        SubscriptionDetailsUiState.Error -> LaunchedEffect(Unit) { closeGuard.resolve(navigator::navigateBack) }

        is SubscriptionDetailsUiState.Loaded -> ExpressiveTheme {
            SubscriptionDetailsContent(
                state = state,
                onClose = { closeGuard.resolve(navigator::navigateBack) },
            )
        }
    }
}
