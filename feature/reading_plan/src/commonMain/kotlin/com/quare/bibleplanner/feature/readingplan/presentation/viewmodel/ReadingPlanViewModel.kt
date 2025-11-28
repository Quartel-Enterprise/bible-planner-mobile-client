package com.quare.bibleplanner.feature.readingplan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.usecase.InitializeBooksIfNeeded
import com.quare.bibleplanner.feature.readingplan.presentation.factory.ReadingPlanStateFactory
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ReadingPlanViewModel(
    factory: ReadingPlanStateFactory,
    private val initializeBooksIfNeeded: InitializeBooksIfNeeded,
) : ViewModel() {
    private val _uiState: MutableStateFlow<ReadingPlanUiState> = MutableStateFlow(factory.createLoading())
    val uiState: StateFlow<ReadingPlanUiState> = _uiState

    init {
        viewModelScope.launch {
            initializeBooksIfNeeded()
            _uiState.update { factory.createLoaded(it.selectedReadingPlan) }
        }
    }

    fun onEvent(event: ReadingPlanUiEvent) {
        when (event) {
            is ReadingPlanUiEvent.OnPlanClick -> _uiState.update { currentUiState ->
                when (currentUiState) {
                    is ReadingPlanUiState.Loaded -> currentUiState.copy(selectedReadingPlan = event.type)
                    is ReadingPlanUiState.Loading -> currentUiState.copy(selectedReadingPlan = event.type)
                }
            }
        }
    }
}
