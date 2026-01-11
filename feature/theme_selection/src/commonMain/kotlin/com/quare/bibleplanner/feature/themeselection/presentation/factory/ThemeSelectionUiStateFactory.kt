package com.quare.bibleplanner.feature.themeselection.presentation.factory

import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.isAndroid
import com.quare.bibleplanner.feature.materialyou.domain.usecase.GetIsDynamicColorsEnabledFlow
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetContrastTypeFlow
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetThemeOptionFlow
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal class ThemeSelectionUiStateFactory(
    private val getThemeOptionFlow: GetThemeOptionFlow,
    private val getIsDynamicColorsEnabledFlow: GetIsDynamicColorsEnabledFlow,
    private val getContrastTypeFlow: GetContrastTypeFlow,
    private val platform: Platform,
) {
    fun create(): Flow<ThemeSelectionUiState> = combine(
        getThemeOptionFlow(),
        getIsDynamicColorsEnabledFlow(),
        getContrastTypeFlow(),
    ) { theme, isDynamicColorEnabled, contrast ->
        ThemeSelectionUiState(
            isMaterialYouToggleOn = isDynamicColorEnabled.takeIf { platform.isAndroid() },
            options = ThemeOptionsFactory.themeOptions.map { themeOption ->
                themeOption.copy(isActive = themeOption.preference == theme)
            },
            selectedContrast = contrast,
        )
    }
}
