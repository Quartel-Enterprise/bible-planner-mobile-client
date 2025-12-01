package com.quare.bibleplanner.feature.materialyou.domain.model

import com.quare.bibleplanner.feature.materialyou.domain.usecase.GetIsDynamicColorsEnabledFlow
import com.quare.bibleplanner.feature.materialyou.domain.usecase.SetIsDynamicColorsEnabled

data class MaterialYouUseCases(
    val getIsDynamicColorsEnabledFlow: GetIsDynamicColorsEnabledFlow,
    val setIsDynamicColorsEnabled: SetIsDynamicColorsEnabled,
)
