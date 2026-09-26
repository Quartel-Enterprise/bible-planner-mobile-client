package com.quare.bibleplanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.inappupdate.domain.usecase.RequestUpdatePromptIfNeeded
import com.quare.bibleplanner.core.model.AppForegroundStateHolder
import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.GetContrastTypeFlow
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.GetThemeOptionFlow
import com.quare.bibleplanner.domain.usecase.InitializeAppContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val requestUpdatePromptIfNeeded: RequestUpdatePromptIfNeeded,
    private val appForegroundStateHolder: AppForegroundStateHolder,
    getThemeOptionFlow: GetThemeOptionFlow,
    getContrastTypeFlow: GetContrastTypeFlow,
    initializeAppContent: InitializeAppContent,
) : ViewModel() {
    init {
        initializeAppContent(viewModelScope)
    }

    val themeState: StateFlow<Theme> = getThemeOptionFlow().toStateFlow(Theme.SYSTEM)

    val contrastState: StateFlow<ContrastType> = getContrastTypeFlow().toStateFlow(ContrastType.Standard)

    fun onAppForegrounded() {
        appForegroundStateHolder.onForegrounded()
        viewModelScope.launch {
            requestUpdatePromptIfNeeded()
        }
    }

    fun onAppBackgrounded() {
        appForegroundStateHolder.onBackgrounded()
    }

    private fun <T> Flow<T>.toStateFlow(initialValue: T): StateFlow<T> = stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = initialValue,
    )
}
