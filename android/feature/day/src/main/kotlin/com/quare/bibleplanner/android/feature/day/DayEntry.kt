package com.quare.bibleplanner.android.feature.day

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.getDayStudyMainPane
import com.quare.bibleplanner.feature.day.presentation.model.DayMessage
import com.quare.bibleplanner.feature.day.presentation.model.DayUiAction
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.viewmodel.DayViewModel
import com.quare.bibleplanner.feature.daystudy.presentation.component.DayStudySection
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun DayNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    // Why: the study panel scene puts the day study beside an entry that carries this metadata on wide screens.
    metadata = getDayStudyMainPane(),
) {
    val viewModel = koinViewModel<DayViewModel> { parametersOf(this) }
    val uiState by viewModel.uiState.collectAsState()
    val onEvent = viewModel::onEvent
    val isWide = LocalIsWideLayout.current
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    LaunchedEffect(isWide) {
        onEvent(DayUiEvent.OnWidthClassChanged(isWide))
    }
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is DayUiAction.ShowSnackBar -> snackbarHostState.showSnackbar(context.getMessage(action.message))
            is DayUiAction.ShowSnackBarText -> snackbarHostState.showSnackbar(action.message)
            DayUiAction.ClearFocus -> focusManager.clearFocus()
        }
    }
    ExpressiveTheme {
        DayContent(
            state = uiState,
            isWide = isWide,
            snackbarHostState = snackbarHostState,
            onEvent = onEvent,
            studySection = { loaded, modifier ->
                DayStudySection(
                    passages = loaded.day.passages,
                    dayRoute = loaded.dayRoute,
                    onOpenPaywall = { onEvent(DayUiEvent.OnDayStudySubscribeClick) },
                    onOpenLoginWarning = { onEvent(DayUiEvent.OnDayStudyLoginRequired) },
                    onShowSnackBar = { message -> onEvent(DayUiEvent.OnDayStudyMessage(message)) },
                    onNavigateToStudy = { onEvent(DayUiEvent.OnDayStudyNavigate) },
                    modifier = modifier,
                )
            },
        )
    }
}

private fun Context.getMessage(message: DayMessage): String = getString(message.toStringRes())

@StringRes
private fun DayMessage.toStringRes(): Int = when (this) {
    DayMessage.FAILED_TO_TOGGLE_CHAPTER -> R.string.day_failed_to_toggle_chapter
    DayMessage.NOTHING_TO_DELETE -> R.string.day_nothing_to_delete
}
