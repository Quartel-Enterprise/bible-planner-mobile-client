package com.quare.bibleplanner.feature.deletenotes.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun DeleteNotesUiActionCollector(
    uiActionFlow: Flow<Unit>,
    onNavigateBack: () -> Unit,
) {
    ActionCollector(uiActionFlow) {
        onNavigateBack()
    }
}
