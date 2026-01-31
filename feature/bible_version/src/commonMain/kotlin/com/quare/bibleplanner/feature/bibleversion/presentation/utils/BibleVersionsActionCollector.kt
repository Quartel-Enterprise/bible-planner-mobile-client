package com.quare.bibleplanner.feature.bibleversion.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun BibleVersionsActionCollector(
    navHostController: NavHostController,
    uiActionFlow: Flow<BibleVersionUiAction>,
) {
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            BibleVersionUiAction.BackToPreviousRoute -> navHostController.navigateUp()
        }
    }
}
