package com.quare.bibleplanner.feature.readingplan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.usecase.CalculateBibleProgressUseCase
import com.quare.bibleplanner.core.books.domain.usecase.InitializeBooksIfNeeded
import com.quare.bibleplanner.core.model.plan.PlansModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.UpdateDayReadStatusUseCase
import com.quare.bibleplanner.feature.readingplan.domain.usecase.FindFirstWeekWithUnreadBook
import com.quare.bibleplanner.feature.readingplan.domain.usecase.GetSelectedReadingPlanFlow
import com.quare.bibleplanner.feature.readingplan.domain.usecase.SetSelectedReadingPlan
import com.quare.bibleplanner.feature.readingplan.presentation.factory.ReadingPlanStateFactory
import com.quare.bibleplanner.feature.readingplan.presentation.model.OverflowOption
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiAction
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel
import com.quare.bibleplanner.ui.utils.observe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ReadingPlanViewModel(
    factory: ReadingPlanStateFactory,
    getPlansByWeek: GetPlansByWeekUseCase,
    getSelectedReadingPlanFlow: GetSelectedReadingPlanFlow,
    private val initializeBooksIfNeeded: InitializeBooksIfNeeded,
    private val updateDayReadStatus: UpdateDayReadStatusUseCase,
    private val setSelectedReadingPlan: SetSelectedReadingPlan,
    private val findFirstWeekWithUnreadBook: FindFirstWeekWithUnreadBook,
    calculateBibleProgressUseCase: CalculateBibleProgressUseCase,
) : ViewModel() {
    private val _uiState: MutableStateFlow<ReadingPlanUiState> = MutableStateFlow(factory.createFirstState())
    val uiState: StateFlow<ReadingPlanUiState> = _uiState

    private val _uiAction: MutableSharedFlow<ReadingPlanUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<ReadingPlanUiAction> = _uiAction

    private var currentPlansModel: PlansModel? = null
    private var currentBibleProgress: Float = 0f
    private val expandedWeeks = mutableSetOf<Int>()
    private var isFirstLoad = true

    init {
        viewModelScope.launch {
            initializeBooksIfNeeded()
        }
        observe(calculateBibleProgressUseCase()) { progress ->
            currentBibleProgress = progress
            _uiState.update { currentState ->
                when (currentState) {
                    is ReadingPlanUiState.Loaded -> {
                        currentState.copy(progress = progress)
                    }

                    is ReadingPlanUiState.Loading -> {
                        currentState
                    }
                }
            }
        }
        observe(getSelectedReadingPlanFlow()) { selectedPlan ->
            _uiState.update { currentState ->
                val plansModel = currentPlansModel
                if (plansModel != null) {
                    val selectedWeeks = when (selectedPlan) {
                        ReadingPlanType.CHRONOLOGICAL -> plansModel.chronologicalOrder
                        ReadingPlanType.BOOKS -> plansModel.booksOrder
                    }
                    val weekPresentationModels = createWeekPresentationModels(selectedWeeks)

                    ReadingPlanUiState.Loaded(
                        weekPlans = weekPresentationModels,
                        progress = currentBibleProgress,
                        selectedReadingPlan = selectedPlan,
                        isShowingMenu = currentState.isShowingMenu,
                        scrollToWeekNumber = currentState.scrollToWeekNumber,
                        scrollToTop = currentState.scrollToTop,
                        isScrolledDown = currentState.isScrolledDown,
                    )
                } else {
                    when (currentState) {
                        is ReadingPlanUiState.Loaded -> {
                            currentState.copy(
                                selectedReadingPlan = selectedPlan,
                            )
                        }

                        is ReadingPlanUiState.Loading -> {
                            currentState.copy(
                                selectedReadingPlan = selectedPlan,
                            )
                        }
                    }
                }
            }
        }
        observe(getPlansByWeek()) { plansModel ->
            currentPlansModel = plansModel
            _uiState.update { currentState ->
                val selectedPlan = currentState.selectedReadingPlan
                val selectedWeeks = when (selectedPlan) {
                    ReadingPlanType.CHRONOLOGICAL -> plansModel.chronologicalOrder
                    ReadingPlanType.BOOKS -> plansModel.booksOrder
                }

                // On first load, find and expand the first week with unread books
                var scrollToWeekNumber = currentState.scrollToWeekNumber
                if (isFirstLoad) {
                    val firstUnreadWeekNumber = findFirstWeekWithUnreadBook(selectedWeeks)
                    if (firstUnreadWeekNumber != null) {
                        expandedWeeks.add(firstUnreadWeekNumber)
                        // Always set scrollToWeekNumber - Root will handle skipping scroll for week 1 if at top
                        scrollToWeekNumber = firstUnreadWeekNumber
                    }
                    isFirstLoad = false
                }

                val weekPresentationModels = createWeekPresentationModels(selectedWeeks)

                ReadingPlanUiState.Loaded(
                    weekPlans = weekPresentationModels,
                    progress = currentBibleProgress,
                    selectedReadingPlan = selectedPlan,
                    isShowingMenu = uiState.value.isShowingMenu,
                    scrollToWeekNumber = scrollToWeekNumber,
                    scrollToTop = currentState.scrollToTop,
                    isScrolledDown = currentState.isScrolledDown,
                )
            }
        }
    }

    fun onEvent(event: ReadingPlanUiEvent) {
        when (event) {
            is ReadingPlanUiEvent.OnPlanClick -> {
                viewModelScope.launch {
                    setSelectedReadingPlan(event.type)
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

                            currentUiState.copy(
                                weekPlans = weekPresentationModels,
                                scrollToWeekNumber = currentUiState.scrollToWeekNumber,
                                scrollToTop = currentUiState.scrollToTop,
                                isScrolledDown = currentUiState.isScrolledDown,
                            )
                        }

                        is ReadingPlanUiState.Loading -> {
                            currentUiState
                        }
                    }
                }
            }

            is ReadingPlanUiEvent.OnDayReadClick -> {
                val currentUiState = _uiState.value

                if (currentUiState !is ReadingPlanUiState.Loaded) {
                    return
                }

                val week = currentPlansModel?.let { plansModel ->
                    val selectedWeeks = when (currentUiState.selectedReadingPlan) {
                        ReadingPlanType.CHRONOLOGICAL -> plansModel.chronologicalOrder
                        ReadingPlanType.BOOKS -> plansModel.booksOrder
                    }
                    selectedWeeks.find { it.number == event.weekNumber }
                } ?: return

                val day = week.days.find { it.number == event.dayNumber } ?: return
                val newReadStatus = !day.isRead

                viewModelScope.launch {
                    updateDayReadStatus(
                        weekNumber = event.weekNumber,
                        dayNumber = event.dayNumber,
                        isRead = newReadStatus,
                        readingPlanType = currentUiState.selectedReadingPlan,
                    )
                }
            }

            is ReadingPlanUiEvent.OnDayClick -> {
                emitUiAction(
                    ReadingPlanUiAction.GoToDay(
                        dayNumber = event.dayNumber,
                        weekNumber = event.weekNumber,
                        readingPlanType = uiState.value.selectedReadingPlan,
                    ),
                )
            }

            ReadingPlanUiEvent.OnOverflowClick -> {
                changeMenuVisibility(true)
            }

            ReadingPlanUiEvent.OnOverflowDismiss -> {
                changeMenuVisibility(false)
            }

            is ReadingPlanUiEvent.OnOverflowOptionClick -> {
                changeMenuVisibility(false)
                emitUiAction(event.option.toUiAction())
            }

            ReadingPlanUiEvent.OnScrollToFirstUnreadWeekClick -> {
                val currentUiState = _uiState.value
                val plansModel = currentPlansModel

                if (currentUiState !is ReadingPlanUiState.Loaded || plansModel == null) {
                    return
                }

                val selectedWeeks = when (currentUiState.selectedReadingPlan) {
                    ReadingPlanType.CHRONOLOGICAL -> plansModel.chronologicalOrder
                    ReadingPlanType.BOOKS -> plansModel.booksOrder
                }

                val firstUnreadWeekNumber = findFirstWeekWithUnreadBook(selectedWeeks)
                if (firstUnreadWeekNumber != null) {
                    // Expand the week if it's not already expanded
                    if (!expandedWeeks.contains(firstUnreadWeekNumber)) {
                        expandedWeeks.add(firstUnreadWeekNumber)
                        _uiState.update { state ->
                            if (state is ReadingPlanUiState.Loaded) {
                                val weekPresentationModels = createWeekPresentationModels(
                                    state.weekPlans.map { it.weekPlan },
                                )
                                state.copy(
                                    weekPlans = weekPresentationModels,
                                    scrollToWeekNumber = state.scrollToWeekNumber,
                                    scrollToTop = state.scrollToTop,
                                    isScrolledDown = state.isScrolledDown,
                                )
                            } else {
                                state
                            }
                        }
                    }
                    // Scroll to the week
                    _uiState.update { state ->
                        when (state) {
                            is ReadingPlanUiState.Loaded -> state.copy(scrollToWeekNumber = firstUnreadWeekNumber)
                            is ReadingPlanUiState.Loading -> state.copy(scrollToWeekNumber = firstUnreadWeekNumber)
                        }
                    }
                }
            }

            ReadingPlanUiEvent.OnScrollToTopClick -> {
                _uiState.update { state ->
                    when (state) {
                        is ReadingPlanUiState.Loaded -> state.copy(scrollToTop = true)
                        is ReadingPlanUiState.Loading -> state.copy(scrollToTop = true)
                    }
                }
            }

            is ReadingPlanUiEvent.OnScrollStateChange -> {
                _uiState.update { state ->
                    when (state) {
                        is ReadingPlanUiState.Loaded -> state.copy(isScrolledDown = event.isScrolledDown)
                        is ReadingPlanUiState.Loading -> state.copy(isScrolledDown = event.isScrolledDown)
                    }
                }
            }

            ReadingPlanUiEvent.OnScrollToWeekCompleted -> {
                _uiState.update { state ->
                    when (state) {
                        is ReadingPlanUiState.Loaded -> state.copy(scrollToWeekNumber = 0)
                        is ReadingPlanUiState.Loading -> state.copy(scrollToWeekNumber = 0)
                    }
                }
            }

            ReadingPlanUiEvent.OnScrollToTopCompleted -> {
                _uiState.update { state ->
                    when (state) {
                        is ReadingPlanUiState.Loaded -> state.copy(scrollToTop = false)
                        is ReadingPlanUiState.Loading -> state.copy(scrollToTop = false)
                    }
                }
            }
        }
    }

    private fun changeMenuVisibility(isShowing: Boolean) {
        _uiState.update { currentUiState ->
            when (currentUiState) {
                is ReadingPlanUiState.Loaded -> {
                    currentUiState.copy(
                        isShowingMenu = isShowing,
                        scrollToWeekNumber = currentUiState.scrollToWeekNumber,
                        scrollToTop = currentUiState.scrollToTop,
                        isScrolledDown = currentUiState.isScrolledDown,
                    )
                }

                is ReadingPlanUiState.Loading -> {
                    currentUiState.copy(
                        isShowingMenu = isShowing,
                        scrollToWeekNumber = currentUiState.scrollToWeekNumber,
                        scrollToTop = currentUiState.scrollToTop,
                        isScrolledDown = currentUiState.isScrolledDown,
                    )
                }
            }
        }
    }

    private fun OverflowOption.toUiAction(): ReadingPlanUiAction = when (this) {
        OverflowOption.THEME -> ReadingPlanUiAction.GoToTheme
        OverflowOption.DELETE_PROGRESS -> ReadingPlanUiAction.GoToDeleteAllProgress
        OverflowOption.PRIVACY_POLICY -> ReadingPlanUiAction.OpenLink("$BASE_URL/privacy")
        OverflowOption.TERMS -> ReadingPlanUiAction.OpenLink("$BASE_URL/terms")
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

    private fun emitUiAction(uiAction: ReadingPlanUiAction) {
        viewModelScope.launch {
            _uiAction.emit(uiAction)
        }
    }

    companion object {
        private const val BASE_URL = "https://www.bibleplanner.app"
    }
}
