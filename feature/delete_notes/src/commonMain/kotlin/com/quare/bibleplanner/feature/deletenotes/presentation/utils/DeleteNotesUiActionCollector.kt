package com.quare.bibleplanner.feature.deletenotes.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun DeleteNotesUiActionCollector(
    uiActionFlow: Flow<Unit>,
    navController: NavHostController,
) {
    ActionCollector(uiActionFlow) {
        navController.navigateUp()
    }
}
