package com.quare.bibleplanner.android.feature.versenote

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.VerseNoteNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.verse.addnote.presentation.VerseNoteViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun VerseNoteNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<VerseNoteViewModel> { parametersOf(this) }
    val uiState by viewModel.uiState.collectAsState()
    ExpressiveTheme {
        VerseNoteContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
