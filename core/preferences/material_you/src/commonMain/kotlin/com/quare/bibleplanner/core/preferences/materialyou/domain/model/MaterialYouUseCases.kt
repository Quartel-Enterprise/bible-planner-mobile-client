package com.quare.bibleplanner.core.preferences.materialyou.domain.model

import com.quare.bibleplanner.core.preferences.materialyou.domain.usecase.GetIsDynamicColorsEnabledFlow
import com.quare.bibleplanner.core.preferences.materialyou.domain.usecase.SetIsDynamicColorsEnabled

data class MaterialYouUseCases(
    val getIsDynamicColorsEnabledFlow: GetIsDynamicColorsEnabledFlow,
    val setIsDynamicColorsEnabled: SetIsDynamicColorsEnabled,
)
