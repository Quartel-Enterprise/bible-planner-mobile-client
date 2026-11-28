package com.quare.bibleplanner.android.feature.accountdetails

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.AccountDetailsNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsMessage
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsUiAction
import com.quare.bibleplanner.feature.accountdetails.presentation.viewmodel.AccountDetailsViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

internal fun AccountDetailsNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<AccountDetailsViewModel>()
    val navigator = koinInject<Navigator>()
    val appSnackbarController = koinInject<AppSnackbarController>()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    ActionCollector(viewModel.uiAction) { uiAction ->
        when (uiAction) {
            is AccountDetailsUiAction.ShowSnackbar -> appSnackbarController.show(
                context.createSnackbarMessage(uiAction.message),
            )
        }
    }
    ExpressiveTheme {
        AccountDetailsContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onClose = navigator::navigateBack,
        )
    }
}

private fun Context.createSnackbarMessage(message: AccountDetailsMessage): AppSnackbarMessage = AppSnackbarMessage.Text(
    text = getString(message.toStringRes()),
    isDismissible = true,
)

@StringRes
private fun AccountDetailsMessage.toStringRes(): Int = when (this) {
    AccountDetailsMessage.DEVICE_SIGNED_OUT -> R.string.account_details_device_signed_out
    AccountDetailsMessage.ACTION_ERROR -> R.string.account_details_action_error
}
