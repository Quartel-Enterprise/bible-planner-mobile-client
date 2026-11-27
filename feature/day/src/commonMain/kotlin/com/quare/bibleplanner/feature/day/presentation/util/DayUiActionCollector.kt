package com.quare.bibleplanner.feature.day.presentation.util

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.failed_to_toggle_chapter_message
import bibleplanner.feature.day.generated.resources.nothing_to_delete_message
import com.quare.bibleplanner.feature.day.presentation.model.DayMessage
import com.quare.bibleplanner.feature.day.presentation.model.DayUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

@Composable
internal fun DayUiActionCollector(
    uiActionFlow: Flow<DayUiAction>,
    snackbarHostState: SnackbarHostState,
) {
    val focusManager = LocalFocusManager.current
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            is DayUiAction.ShowSnackBar -> snackbarHostState.showSnackbar(getString(action.message.toStringResource()))
            is DayUiAction.ShowSnackBarText -> snackbarHostState.showSnackbar(action.message)
            DayUiAction.ClearFocus -> focusManager.clearFocus()
        }
    }
}

private fun DayMessage.toStringResource(): StringResource = when (this) {
    DayMessage.FAILED_TO_TOGGLE_CHAPTER -> Res.string.failed_to_toggle_chapter_message
    DayMessage.NOTHING_TO_DELETE -> Res.string.nothing_to_delete_message
}
