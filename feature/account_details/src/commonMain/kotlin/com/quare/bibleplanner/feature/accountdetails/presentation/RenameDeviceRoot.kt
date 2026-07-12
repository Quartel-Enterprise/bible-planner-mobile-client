package com.quare.bibleplanner.feature.accountdetails.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.RenameDeviceNavRoute
import com.quare.bibleplanner.feature.accountdetails.presentation.content.RenameDeviceDialog
import com.quare.bibleplanner.feature.accountdetails.presentation.viewmodel.RenameDeviceViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.renameDevice(onNavigateBack: () -> Unit) {
    entry<RenameDeviceNavRoute>(metadata = DialogSceneStrategy.dialog()) { route ->
        val viewModel: RenameDeviceViewModel = koinViewModel { parametersOf(route) }
        ActionCollector(viewModel.backUiAction) { onNavigateBack() }
        RenameDeviceDialog(
            initialName = viewModel.currentName,
            onEvent = viewModel::onEvent,
        )
    }
}
