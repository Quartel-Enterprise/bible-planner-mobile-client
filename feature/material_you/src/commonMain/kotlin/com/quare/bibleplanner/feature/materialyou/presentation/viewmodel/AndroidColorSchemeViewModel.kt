package com.quare.bibleplanner.feature.materialyou.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.utils.orFalse
import com.quare.bibleplanner.core.preferences.materialyou.domain.model.MaterialYouUseCases
import com.quare.bibleplanner.feature.materialyou.presentation.model.AndroidColorSchemeUiEvent
import com.quare.bibleplanner.ui.utils.observe
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AndroidColorSchemeViewModel(
    private val useCases: MaterialYouUseCases,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<AndroidColorSchemeUiEvent>(trackEvent) {
    val uiState: StateFlow<Boolean>
        field = MutableStateFlow(false)

    init {
        observe(
            flow = useCases.getIsDynamicColorsEnabledFlow(),
            collector = {
                uiState.value = it.orFalse()
            },
        )
    }

    override fun handleEvent(event: AndroidColorSchemeUiEvent) {
        viewModelScope.launch {
            when (event) {
                is AndroidColorSchemeUiEvent.OnIsDynamicColorsEnabledChange ->
                    useCases.setIsDynamicColorsEnabled(event.isEnabled)

                AndroidColorSchemeUiEvent.OnInformationDialogDismiss,
                AndroidColorSchemeUiEvent.BottomSheetGotItClick,
                -> navigator.navigateBack()
            }
        }
    }
}
