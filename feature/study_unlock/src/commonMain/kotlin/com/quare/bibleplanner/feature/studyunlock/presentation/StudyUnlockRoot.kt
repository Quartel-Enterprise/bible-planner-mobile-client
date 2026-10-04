package com.quare.bibleplanner.feature.studyunlock.presentation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.studyunlock.presentation.content.StudyUnlockSheet
import com.quare.bibleplanner.feature.studyunlock.presentation.model.StudyUnlockUiEvent
import com.quare.bibleplanner.feature.studyunlock.presentation.viewmodel.StudyUnlockViewModel
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.studyUnlock() {
    entry<StudyUnlockNavRoute>(metadata = getSheetPane()) { route ->
        val viewModel = koinViewModel<StudyUnlockViewModel> { parametersOf(route) }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ResponsiveDialogSheet(
            onCloseClick = { viewModel.onEvent(StudyUnlockUiEvent.OnDismiss) },
        ) {
            StudyUnlockSheet(
                uiState = uiState,
                onEvent = viewModel::onEvent,
            )
        }
    }
}
