package com.quare.bibleplanner.feature.readingplan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.quare.bibleplanner.feature.readingplan.presentation.factory.ReadingPlanStateFactory
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal class ReadingPlanViewModel(
    factory: ReadingPlanStateFactory,
) : ViewModel() {
    private val _uiState: MutableStateFlow<ReadingPlanUiState> = MutableStateFlow(factory.create())
    val uiState: StateFlow<ReadingPlanUiState> = _uiState

    fun onEvent(event: ReadingPlanUiEvent) {
        when (event) {
            is ReadingPlanUiEvent.OnPlanClick -> _uiState.update { it.copy(selectedReadingPlan = event.type) }
        }
    }
}
