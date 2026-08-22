package com.quare.bibleplanner.feature.verse.addnote.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun VerseNoteUiActionCollector(
    uiActionFlow: Flow<Unit>,
    onNavigateBack: () -> Unit,
) {
    ActionCollector(uiActionFlow) {
        onNavigateBack()
    }
}
