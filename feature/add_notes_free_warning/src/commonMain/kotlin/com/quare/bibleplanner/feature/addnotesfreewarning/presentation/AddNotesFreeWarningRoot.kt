package com.quare.bibleplanner.feature.addnotesfreewarning.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.utils.AddNotesFreeWarningUiActionCollector
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.viewmodel.AddNotesFreeWarningViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.addNotesFreeWarning(navController: NavHostController) {
    dialog<AddNotesFreeWarningNavRoute> {
        val viewModel = koinViewModel<AddNotesFreeWarningViewModel>()
        val maxFreeNotesAmount = viewModel.maxFreeNotesAmount
        AddNotesFreeWarningUiActionCollector(
            maxFreeNotesAmount = maxFreeNotesAmount,
            uiActionFlow = viewModel.uiAction,
            navController = navController,
        )
        AddNotesFreeWarningDialog(
            maxFreeNotesAmount = maxFreeNotesAmount,
            onEvent = viewModel::onEvent,
        )
    }
}

