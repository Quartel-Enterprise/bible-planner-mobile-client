package com.quare.bibleplanner.feature.congrats.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.CongratsNavRoute
import com.quare.bibleplanner.feature.congrats.presentation.viewmodel.CongratsViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.congrats() {
    entry<CongratsNavRoute>(metadata = DialogSceneStrategy.dialog()) {
        val viewModel: CongratsViewModel = koinViewModel()
        val onEvent = viewModel::onEvent

        CongratsBottomSheet(
            onEvent = onEvent,
        )
    }
}
