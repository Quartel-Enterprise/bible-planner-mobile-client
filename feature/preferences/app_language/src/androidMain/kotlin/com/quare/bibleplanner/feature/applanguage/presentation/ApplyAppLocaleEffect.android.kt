package com.quare.bibleplanner.feature.applanguage.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.feature.applanguage.presentation.utils.applyAppLanguageLocale
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import org.koin.compose.koinInject

@Composable
actual fun ApplyAppLocaleEffect() {
    val getAppLanguageFlow: GetAppLanguageFlow = koinInject()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        // Skip the current value: it was already applied via attachBaseContext at launch. Only react
        // to later changes (user choice or a synced update from another device).
        getAppLanguageFlow()
            .distinctUntilChanged()
            .drop(1)
            .collect { language -> context.applyAppLanguageLocale(language) }
    }
}
