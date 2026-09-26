package com.quare.bibleplanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.preferences.materialyou.domain.usecase.GetIsDynamicColorsEnabledFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainActivityViewModel(
    val navigator: Navigator,
    getIsDynamicColorsEnabledFlow: GetIsDynamicColorsEnabledFlow,
) : ViewModel() {
    val isDynamicColorsEnabled: StateFlow<Boolean> = getIsDynamicColorsEnabledFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false,
    )
}
