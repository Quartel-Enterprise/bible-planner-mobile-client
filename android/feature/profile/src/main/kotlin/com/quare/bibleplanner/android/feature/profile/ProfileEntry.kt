package com.quare.bibleplanner.android.feature.profile

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.android.navigation.LocalMainEntryAnimatedContentScope
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileMessage
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiAction
import com.quare.bibleplanner.feature.profile.presentation.viewmodel.ProfileViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.LocalHostedTabBarHeight
import com.quare.bibleplanner.ui.utils.ReserveHostedTabBarHeightEffect
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import com.quare.bibleplanner.ui.utils.toClipEntry
import com.quare.bibleplanner.ui.utils.transition.LocalSharedTransitionScope
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

internal fun MainNavRouteDestination.Profile.toNavEntry(): NavEntry<NavKey> = NavEntry(key = this) {
    ProfileTabEntryContent()
}

@Composable
private fun ProfileTabEntryContent() {
    val viewModel = koinViewModel<ProfileViewModel>()
    val state by viewModel.uiState.collectAsState()
    ProfileUiActionCollector(viewModel = viewModel)
    ReserveHostedTabBarHeightEffect()
    val transitionScopes = rememberProfileTransitionScopes()
    ExpressiveTheme {
        ProfileContent(
            state = state,
            transitionScopes = transitionScopes,
            hostedTabBarHeight = LocalHostedTabBarHeight.current ?: 0.dp,
            onEvent = viewModel::onEvent,
        )
    }
}

@Composable
private fun ProfileUiActionCollector(viewModel: ProfileViewModel) {
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val appSnackbarController = koinInject<AppSnackbarController>()
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is ProfileUiAction.OpenLink -> uriHandler.openUri(action.url)

            ProfileUiAction.ShowNoProgressToDelete -> appSnackbarController.show(
                context.createSnackbarMessage(R.string.profile_no_progress_to_delete_message),
            )

            is ProfileUiAction.ShowSnackbar -> appSnackbarController.show(
                context.createSnackbarMessage(action.message.toStringRes()),
            )

            is ProfileUiAction.Copy -> clipboard.setClipEntry(action.text.toClipEntry())
        }
    }
}

private fun Context.createSnackbarMessage(textRes: Int): AppSnackbarMessage = AppSnackbarMessage.Text(
    text = getString(textRes),
    isDismissible = false,
)

private fun ProfileMessage.toStringRes(): Int = when (this) {
    ProfileMessage.DELETE_ACCOUNT_REQUIRES_INTERNET -> R.string.profile_delete_account_requires_internet
    ProfileMessage.LOGIN_REQUIRES_INTERNET -> R.string.profile_login_requires_internet
    ProfileMessage.LOGOUT_REQUIRES_INTERNET -> R.string.profile_logout_requires_internet
    ProfileMessage.UP_TO_DATE -> R.string.profile_up_to_date_message
    ProfileMessage.UPDATE_CHECK_FAILED -> R.string.profile_update_check_failed_message
}

@Composable
private fun rememberProfileTransitionScopes(): ProfileTransitionScopes? {
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return null
    val animatedVisibilityScope = LocalMainEntryAnimatedContentScope.current ?: return null
    return remember(sharedTransitionScope, animatedVisibilityScope) {
        ProfileTransitionScopes(
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }
}
