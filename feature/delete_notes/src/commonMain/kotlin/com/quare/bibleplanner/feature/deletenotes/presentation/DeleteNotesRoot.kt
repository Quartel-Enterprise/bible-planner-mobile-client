package com.quare.bibleplanner.feature.deletenotes.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute
import com.quare.bibleplanner.feature.deletenotes.presentation.utils.DeleteNotesUiActionCollector
import com.quare.bibleplanner.feature.deletenotes.presentation.viewmodel.DeleteNotesViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.deleteNotes(navController: NavHostController) {
    dialog<DeleteNotesRoute> {
        val viewModel = koinViewModel<DeleteNotesViewModel>()
        DeleteNotesUiActionCollector(
            uiActionFlow = viewModel.backUiAction,
            navController = navController,
        )
        DeleteNotesDialog(
            onEvent = viewModel::onEvent,
        )
    }
}
