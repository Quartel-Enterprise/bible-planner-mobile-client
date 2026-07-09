package com.quare.bibleplanner.feature.deleteprogress.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun DeleteProgressUiActionCollector(
    uiActionFlow: Flow<Unit>,
    onNavigateBack: () -> Unit,
) {
    ActionCollector(uiActionFlow) {
        onNavigateBack()
    }
}
