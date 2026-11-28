package com.quare.bibleplanner.android.feature.materialyou

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.android.designsystem.dialog.HideSceneDialogScrimEffect
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.MaterialYouBottomSheetNavRoute
import com.quare.bibleplanner.feature.materialyou.presentation.viewmodel.AndroidColorSchemeViewModel
import org.koin.compose.viewmodel.koinViewModel

internal fun MaterialYouBottomSheetNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = DialogSceneStrategy.dialog(),
) {
    val viewModel = koinViewModel<AndroidColorSchemeViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    // Why: the scene window shows while the state loads, and its scrim must not flash before the dialog's.
    HideSceneDialogScrimEffect()
    uiState?.let { isDynamicColorsEnabled ->
        ExpressiveTheme {
            MaterialYouContent(
                isDynamicColorsEnabled = isDynamicColorsEnabled,
                onEvent = viewModel::onEvent,
            )
        }
    }
}
