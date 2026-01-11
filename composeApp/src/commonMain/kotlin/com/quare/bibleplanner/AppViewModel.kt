package com.quare.bibleplanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetContrastTypeFlow
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetThemeOptionFlow
import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AppViewModel(
    getThemeOptionFlow: GetThemeOptionFlow,
    getContrastTypeFlow: GetContrastTypeFlow,
) : ViewModel() {
    val themeState: StateFlow<Theme> =
        getThemeOptionFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Theme.SYSTEM,
            )

    val contrastState: StateFlow<ContrastType> =
        getContrastTypeFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ContrastType.Standard,
            )
}
