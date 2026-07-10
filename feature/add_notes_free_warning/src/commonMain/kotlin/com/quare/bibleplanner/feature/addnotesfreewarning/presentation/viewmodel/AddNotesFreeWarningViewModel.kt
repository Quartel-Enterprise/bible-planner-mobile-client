package com.quare.bibleplanner.feature.addnotesfreewarning.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model.AddNotesFreeWarningUiAction
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model.AddNotesFreeWarningUiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class AddNotesFreeWarningViewModel(
    route: AddNotesFreeWarningNavRoute,
    private val trackEvent: TrackEvent,
) : ViewModel() {
    val maxFreeNotesAmount = route.maxFreeNotesAmount

    private val _uiAction: MutableSharedFlow<AddNotesFreeWarningUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<AddNotesFreeWarningUiAction> = _uiAction

    fun onEvent(event: AddNotesFreeWarningUiEvent) {
        if (event is AddNotesFreeWarningUiEvent.OnSubscribeToPro) {
            trackEvent(
                name = AnalyticsEventNames.PAYWALL_VIEWED,
                params = mapOf(AnalyticsParams.SOURCE to SOURCE_NOTES_LIMIT),
            )
        }
        viewModelScope.launch {
            _uiAction.emit(event.toUiAction())
        }
    }

    private fun AddNotesFreeWarningUiEvent.toUiAction() = when (this) {
        AddNotesFreeWarningUiEvent.OnCancel -> AddNotesFreeWarningUiAction.NavigateBack
        AddNotesFreeWarningUiEvent.OnSubscribeToPro -> AddNotesFreeWarningUiAction.NavigateToPro
    }

    private companion object {
        const val SOURCE_NOTES_LIMIT = "notes_limit"
    }
}
