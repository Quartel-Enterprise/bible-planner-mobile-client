package com.quare.bibleplanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.feature.materialyou.domain.usecase.GetIsDynamicColorsEnabledFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainActivityViewModel(
    getIsDynamicColorsEnabledFlow: GetIsDynamicColorsEnabledFlow,
    val navigationEventBus: NavigationEventBus,
) : ViewModel() {
    val isDynamicColorsEnabled: StateFlow<Boolean> = getIsDynamicColorsEnabledFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false,
    )
}
