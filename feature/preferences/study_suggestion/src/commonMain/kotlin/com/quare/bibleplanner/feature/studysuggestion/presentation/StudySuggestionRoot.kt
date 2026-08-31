package com.quare.bibleplanner.feature.studysuggestion.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import bibleplanner.feature.preferences.study_suggestion.generated.resources.Res
import bibleplanner.feature.preferences.study_suggestion.generated.resources.study_suggestion_title
import com.quare.bibleplanner.core.model.route.StudySuggestionNavRoute
import com.quare.bibleplanner.feature.studysuggestion.presentation.model.StudySuggestionUiAction
import com.quare.bibleplanner.feature.studysuggestion.presentation.model.StudySuggestionUiEvent
import com.quare.bibleplanner.feature.studysuggestion.presentation.viewmodel.StudySuggestionViewModel
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.studySuggestionSettings() {
    entry<StudySuggestionNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) {
        val viewModel = koinViewModel<StudySuggestionViewModel>()
        val uiState by viewModel.uiState.collectAsState()

        StudySuggestionUiActionCollector(uiActionFlow = viewModel.uiAction)

        ResponsiveDialogSheet(
            onCloseClick = { viewModel.onEvent(StudySuggestionUiEvent.OnDismiss) },
            title = stringResource(Res.string.study_suggestion_title),
        ) {
            StudySuggestionContent(
                uiState = uiState,
                onEvent = viewModel::onEvent,
            )
        }
    }
}

@Composable
private fun StudySuggestionUiActionCollector(uiActionFlow: Flow<StudySuggestionUiAction>) {
    val appSnackbarController = koinInject<AppSnackbarController>()
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            is StudySuggestionUiAction.ShowSnackbar -> {
                appSnackbarController.show(
                    AppSnackbarMessage(
                        stringResource = action.message,
                        isDismissible = true,
                    ),
                )
            }
        }
    }
}
