package com.quare.bibleplanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.domain.usecase.InitializeAppContent
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.RequestUpdatePromptIfNeeded
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetContrastTypeFlow
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetThemeOptionFlow
import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    getThemeOptionFlow: GetThemeOptionFlow,
    getContrastTypeFlow: GetContrastTypeFlow,
    initializeAppContent: InitializeAppContent,
    private val requestUpdatePromptIfNeeded: RequestUpdatePromptIfNeeded,
) : ViewModel() {
    init {
        initializeAppContent(viewModelScope)
    }

    val themeState: StateFlow<Theme> = getThemeOptionFlow().toStateFlow(Theme.SYSTEM)

    val contrastState: StateFlow<ContrastType> = getContrastTypeFlow().toStateFlow(ContrastType.Standard)

    fun onAppForegrounded() {
        viewModelScope.launch {
            requestUpdatePromptIfNeeded()
        }
    }

    private fun <T> Flow<T>.toStateFlow(initialValue: T): StateFlow<T> = stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = initialValue,
    )
}
