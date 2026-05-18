package com.quare.bibleplanner

import androidx.lifecycle.ViewModel
import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.feature.materialyou.domain.usecase.GetIsDynamicColorsEnabledFlow

class MainActivityViewModel(
    getIsDynamicColorsEnabledFlow: GetIsDynamicColorsEnabledFlow,
    val navigationEventBus: NavigationEventBus,
) : ViewModel() {
    val isDynamicColorsEnabledFlow = getIsDynamicColorsEnabledFlow()
}
