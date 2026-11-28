package com.quare.bibleplanner.android.feature.dayreadingcomplete

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.DayReadingCompleteNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteMessage
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiAction
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.viewmodel.DayReadingCompleteViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun DayReadingCompleteNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<DayReadingCompleteViewModel> { parametersOf(this) }
    val uiState by viewModel.uiState.collectAsState()
    val appSnackbarController = koinInject<AppSnackbarController>()
    val context = LocalContext.current
    ActionCollector(viewModel.uiAction) { uiAction ->
        when (uiAction) {
            is DayReadingCompleteUiAction.ShowSnackBar -> appSnackbarController.show(
                context.createSnackbarMessage(uiAction.message),
            )
        }
    }
    ExpressiveTheme {
        DayReadingCompleteContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}

private fun Context.createSnackbarMessage(message: DayReadingCompleteMessage): AppSnackbarMessage =
    AppSnackbarMessage.Text(
        text = getString(message.toStringRes()),
        isDismissible = true,
    )

@StringRes
private fun DayReadingCompleteMessage.toStringRes(): Int = when (this) {
    DayReadingCompleteMessage.OFFLINE -> R.string.day_reading_complete_offline_message
    DayReadingCompleteMessage.NEVER_SHOW_CONFIRMATION -> R.string.day_reading_complete_never_show_confirmation
}
