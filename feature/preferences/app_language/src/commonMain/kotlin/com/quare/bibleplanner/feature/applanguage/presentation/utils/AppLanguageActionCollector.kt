package com.quare.bibleplanner.feature.applanguage.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun AppLanguageActionCollector(
    actionsFlow: Flow<AppLanguageUiAction>,
    navController: NavHostController,
) {
    val applyLanguage = rememberApplyLanguage()
    ActionCollector(actionsFlow) { action ->
        when (action) {
            AppLanguageUiAction.NavigateUp -> {
                navController.navigateUp()
            }

            is AppLanguageUiAction.ApplyAndNavigateUp -> {
                applyLanguage(action.language)
                navController.navigateUp()
            }
        }
    }
}

@Composable
expect fun rememberApplyLanguage(): (Language) -> Unit
