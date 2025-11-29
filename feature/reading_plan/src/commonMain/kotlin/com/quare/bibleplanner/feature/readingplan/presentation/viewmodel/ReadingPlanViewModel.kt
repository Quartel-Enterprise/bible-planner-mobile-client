package com.quare.bibleplanner.feature.readingplan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.usecase.InitializeBooksIfNeeded
import com.quare.bibleplanner.core.books.domain.usecase.MarkPassagesReadUseCase
import com.quare.bibleplanner.core.model.plan.PlansModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.feature.readingplan.presentation.factory.ReadingPlanStateFactory
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel
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
    private val markPassagesReadUseCase: MarkPassagesReadUseCase,
    getPlansByWeek: GetPlansByWeekUseCase,
) : ViewModel() {
    private val _uiState: MutableStateFlow<ReadingPlanUiState> = MutableStateFlow(factory.createLoading())
    val uiState: StateFlow<ReadingPlanUiState> = _uiState

    private var currentPlansModel: PlansModel? = null
    private val expandedWeeks = mutableSetOf<Int>().apply {
        // Default to week 1 expanded
        add(1)
    }

    init {
        viewModelScope.launch {
            initializeBooksIfNeeded()
        }

        getPlansByWeek()
            .onEach { plansModel ->
                currentPlansModel = plansModel
                _uiState.update { currentState ->
                    val selectedPlan = currentState.selectedReadingPlan
                    val selectedWeeks = when (selectedPlan) {
                        ReadingPlanType.CHRONOLOGICAL -> plansModel.chronologicalOrder
                        ReadingPlanType.BOOKS -> plansModel.booksOrder
                    }
                    val progress = calculateProgress(selectedWeeks)
                    val weekPresentationModels = createWeekPresentationModels(selectedWeeks)

                    ReadingPlanUiState.Loaded(
                        weekPlans = weekPresentationModels,
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
            is ReadingPlanUiEvent.OnPlanClick -> {
                _uiState.update { currentUiState ->
                    when (currentUiState) {
                        is ReadingPlanUiState.Loaded -> {
                            val plansModel = currentPlansModel ?: return@update currentUiState
                            val selectedWeeks = when (event.type) {
                                ReadingPlanType.CHRONOLOGICAL -> plansModel.chronologicalOrder
                                ReadingPlanType.BOOKS -> plansModel.booksOrder
                            }
                            val weekPresentationModels = createWeekPresentationModels(selectedWeeks)

                            currentUiState.copy(
                                weekPlans = weekPresentationModels,
                                selectedReadingPlan = event.type,
                            )
                        }

                        is ReadingPlanUiState.Loading -> {
                            currentUiState.copy(selectedReadingPlan = event.type)
                        }
                    }
                }
            }

            is ReadingPlanUiEvent.OnWeekExpandClick -> {
                _uiState.update { currentUiState ->
                    when (currentUiState) {
                        is ReadingPlanUiState.Loaded -> {
                            if (expandedWeeks.contains(event.weekNumber)) {
                                expandedWeeks.remove(event.weekNumber)
                            } else {
                                expandedWeeks.add(event.weekNumber)
                            }

                            val weekPresentationModels = createWeekPresentationModels(
                                currentUiState.weekPlans.map { it.weekPlan },
                            )

                            currentUiState.copy(weekPlans = weekPresentationModels)
                        }

                        is ReadingPlanUiState.Loading -> {
                            currentUiState
                        }
                    }
                }
            }

            is ReadingPlanUiEvent.OnDayReadClick -> {
                val currentUiState = _uiState.value
                val plansModel = currentPlansModel

                if (currentUiState !is ReadingPlanUiState.Loaded || plansModel == null) {
                    return
                }

                val selectedWeeks = when (currentUiState.selectedReadingPlan) {
                    ReadingPlanType.CHRONOLOGICAL -> plansModel.chronologicalOrder
                    ReadingPlanType.BOOKS -> plansModel.booksOrder
                }

                val week = selectedWeeks.find { it.number == event.weekNumber } ?: return
                val day = week.days.find { it.number == event.dayNumber } ?: return

                viewModelScope.launch {
                    markPassagesReadUseCase(day.passages)
                }
            }
        }
    }

    private fun createWeekPresentationModels(weeks: List<WeekPlanModel>): List<WeekPlanPresentationModel> =
        weeks.map { week ->
            WeekPlanPresentationModel(
                weekPlan = week,
                isExpanded = expandedWeeks.contains(week.number),
                readDaysCount = week.days.count { it.isRead },
                totalDays = week.days.size,
            )
        }

    private fun calculateProgress(weeks: List<WeekPlanModel>): Float {
        if (weeks.isEmpty()) return 0f

        val totalVerses = weeks.sumOf { week ->
            week.days.sumOf { it.totalVerses }
        }
        if (totalVerses == 0) return 0f

        val readVerses = weeks.sumOf { week ->
            week.days.sumOf { it.readVerses }
        }

        return 100 * (readVerses.toFloat() / totalVerses.toFloat())
    }
}
