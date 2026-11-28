package com.quare.bibleplanner.android.feature.studysuggestion

import androidx.annotation.StringRes
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.StudySuggestionNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.studysuggestion.presentation.model.StudySuggestionMessage
import com.quare.bibleplanner.feature.studysuggestion.presentation.model.StudySuggestionUiAction
import com.quare.bibleplanner.feature.studysuggestion.presentation.viewmodel.StudySuggestionViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

internal fun StudySuggestionNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<StudySuggestionViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val appSnackbarController = koinInject<AppSnackbarController>()
    val context = LocalContext.current
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is StudySuggestionUiAction.ShowSnackbar -> appSnackbarController.show(
                AppSnackbarMessage.Text(
                    text = context.getString(action.message.toStringRes()),
                    isDismissible = true,
                ),
            )
        }
    }
    ExpressiveTheme {
        StudySuggestionContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}

@StringRes
private fun StudySuggestionMessage.toStringRes(): Int = when (this) {
    StudySuggestionMessage.ENABLED -> R.string.study_suggestion_enabled_message
    StudySuggestionMessage.DISABLED -> R.string.study_suggestion_disabled_message
    StudySuggestionMessage.MODE_BLOCKED -> R.string.study_suggestion_mode_blocked_message
}
