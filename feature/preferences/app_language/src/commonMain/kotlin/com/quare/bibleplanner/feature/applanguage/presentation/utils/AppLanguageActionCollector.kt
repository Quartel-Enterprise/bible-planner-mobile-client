package com.quare.bibleplanner.feature.applanguage.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun AppLanguageActionCollector(actionsFlow: Flow<AppLanguageUiAction>) {
    val applyLanguage = rememberApplyLanguage()
    ActionCollector(actionsFlow) { action ->
        when (action) {
            is AppLanguageUiAction.ApplyLanguage -> applyLanguage(action.language)
        }
    }
}

@Composable
expect fun rememberApplyLanguage(): (Language) -> Unit
