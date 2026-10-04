package com.quare.bibleplanner.feature.materialyou.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.MaterialYouBottomSheetNavRoute
import com.quare.bibleplanner.feature.materialyou.presentation.component.MaterialYouDialog
import com.quare.bibleplanner.feature.materialyou.presentation.viewmodel.AndroidColorSchemeViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.materialYou() {
    entry<MaterialYouBottomSheetNavRoute>(metadata = DialogSceneStrategy.dialog()) {
        val viewModel = koinViewModel<AndroidColorSchemeViewModel>()
        val state by viewModel.uiState.collectAsState()
        MaterialYouDialog(
            isMaterialYouActivated = state,
            onEvent = viewModel::onEvent,
        )
    }
}
