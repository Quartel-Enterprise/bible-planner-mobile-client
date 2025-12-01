package com.quare.bibleplanner

import androidx.lifecycle.ViewModel
import com.quare.bibleplanner.feature.materialyou.domain.usecase.GetIsDynamicColorsEnabledFlow

class MainActivityViewModel(
    getIsDynamicColorsEnabledFlow: GetIsDynamicColorsEnabledFlow,
) : ViewModel() {
    val isDynamicColorsEnabledFlow = getIsDynamicColorsEnabledFlow()
}
