package com.quare.bibleplanner.feature.editplanstartdate.presentation.model

internal sealed interface EditPlanStartDateUiEvent {
    data object OnDismissDialog : EditPlanStartDateUiEvent
    data class OnDateSelected(val utcDateMillis: Long) : EditPlanStartDateUiEvent
}

