package com.quare.bibleplanner.feature.readingplan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.usecase.InitializeBooksIfNeeded
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.feature.readingplan.presentation.factory.ReadingPlanStateFactory
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ReadingPlanViewModel(
    factory: ReadingPlanStateFactory,
    private val initializeBooksIfNeeded: InitializeBooksIfNeeded,
    getPlansByWeek: GetPlansByWeekUseCase,
) : ViewModel() {
    private val _uiState: MutableStateFlow<ReadingPlanUiState> = MutableStateFlow(factory.createLoading())
    val uiState: StateFlow<ReadingPlanUiState> = _uiState

    init {
        viewModelScope.launch {
            initializeBooksIfNeeded()
        }

        getPlansByWeek()
            .onEach { plansModel ->
                _uiState.update { currentState ->
                    val selectedPlan = currentState.selectedReadingPlan
                    val selectedWeeks = when (selectedPlan) {
                        ReadingPlanType.CHRONOLOGICAL -> plansModel.chronologicalOrder
                        ReadingPlanType.BOOKS -> plansModel.booksOrder
                    }
                    val progress = calculateProgress(selectedWeeks)

                    ReadingPlanUiState.Loaded(
                        plansModel = plansModel,
                        progress = progress,
                        selectedReadingPlan = selectedPlan,
                    )
                }
            }.catch { error ->
                // Handle error - could update UI state to show error
                error.printStackTrace()
            }.launchIn(viewModelScope)
    }

    fun onEvent(event: ReadingPlanUiEvent) {
        when (event) {
            is ReadingPlanUiEvent.OnPlanClick -> _uiState.update { currentUiState ->
                when (currentUiState) {
                    is ReadingPlanUiState.Loaded -> {
                        val selectedWeeks = when (event.type) {
                            ReadingPlanType.CHRONOLOGICAL -> currentUiState.plansModel.chronologicalOrder
                            ReadingPlanType.BOOKS -> currentUiState.plansModel.booksOrder
                        }
                        val progress = calculateProgress(selectedWeeks)

                        currentUiState.copy(
                            selectedReadingPlan = event.type,
                            progress = progress,
                        )
                    }

                    is ReadingPlanUiState.Loading -> {
                        currentUiState.copy(selectedReadingPlan = event.type)
                    }
                }
            }
        }
    }

    private fun calculateProgress(weeks: List<WeekPlanModel>): Float {
        if (weeks.isEmpty()) return 0f

        val totalDays = weeks.sumOf { it.days.size }
        if (totalDays == 0) return 0f

        val readDays = weeks.sumOf { week ->
            week.days.count { it.isRead }
        }

        return readDays.toFloat() / totalDays.toFloat()
    }
}
