package com.quare.bibleplanner.android.feature.applanguage

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.AppLanguageNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.applanguage.presentation.AppLanguageViewModel
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiAction
import com.quare.bibleplanner.feature.applanguage.presentation.utils.rememberApplyLanguage
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

internal fun AppLanguageNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getSheetPane(),
) {
    val viewModel = koinViewModel<AppLanguageViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    // Why: the shared screen applies the locale the same way, so a language change recreates the activity alike.
    val applyLanguage = rememberApplyLanguage()
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is AppLanguageUiAction.ApplyLanguage -> applyLanguage(action.language)
        }
    }
    ExpressiveTheme {
        AppLanguageContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
