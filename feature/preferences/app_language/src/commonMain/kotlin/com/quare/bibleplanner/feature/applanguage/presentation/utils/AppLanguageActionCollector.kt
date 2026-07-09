package com.quare.bibleplanner.feature.applanguage.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun AppLanguageActionCollector(
    actionsFlow: Flow<AppLanguageUiAction>,
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val applyLanguage = rememberApplyLanguage()
    ActionCollector(actionsFlow) { action ->
        when (action) {
            AppLanguageUiAction.NavigateUp -> {
                onNavigateBack()
            }

            is AppLanguageUiAction.ApplyAndNavigateUp -> {
                applyLanguage(action.language)
                onNavigateBack()
            }

            AppLanguageUiAction.NavigateToLoginWarning -> {
                onNavigate(LoginWarningNavRoute(LoginWarningReason.Preferences.Language.key))
            }
        }
    }
}

@Composable
expect fun rememberApplyLanguage(): (Language) -> Unit
