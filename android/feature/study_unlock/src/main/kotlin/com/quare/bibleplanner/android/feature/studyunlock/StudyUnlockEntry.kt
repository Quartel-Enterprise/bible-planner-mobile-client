package com.quare.bibleplanner.android.feature.studyunlock

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.studyunlock.presentation.viewmodel.StudyUnlockViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun StudyUnlockNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<StudyUnlockViewModel> { parametersOf(this) }
    val uiState by viewModel.uiState.collectAsState()
    ExpressiveTheme {
        StudyUnlockContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
