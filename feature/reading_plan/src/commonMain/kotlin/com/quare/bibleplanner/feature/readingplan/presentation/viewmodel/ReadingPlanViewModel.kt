package com.quare.bibleplanner.feature.readingplan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.usecase.CalculateBibleProgressUseCase
import com.quare.bibleplanner.core.books.domain.usecase.InitializeBooksIfNeeded
import com.quare.bibleplanner.core.model.plan.PlansModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlanStartDateFlowUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.SetPlanStartTimeUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.UpdateDayReadStatusUseCase
import com.quare.bibleplanner.feature.readingplan.domain.usecase.FindFirstWeekWithUnreadBook
import com.quare.bibleplanner.feature.readingplan.domain.usecase.GetSelectedReadingPlanFlow
import com.quare.bibleplanner.feature.readingplan.domain.usecase.SetSelectedReadingPlan
import com.quare.bibleplanner.feature.readingplan.presentation.factory.ReadingPlanStateFactory
import com.quare.bibleplanner.feature.readingplan.presentation.mapper.CalculateIsFirstUnreadWeekVisible
import com.quare.bibleplanner.feature.readingplan.presentation.mapper.DeleteProgressMapper
import com.quare.bibleplanner.feature.readingplan.presentation.mapper.WeeksPlanPresentationMapper
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ReadingPlanViewModel(
    factory: ReadingPlanStateFactory,
    getPlansByWeek: GetPlansByWeekUseCase,
    getSelectedReadingPlanFlow: GetSelectedReadingPlanFlow,
    calculateBibleProgress: CalculateBibleProgressUseCase,
    getPlanStartDate: GetPlanStartDateFlowUseCase,
    private val initializeBooksIfNeeded: InitializeBooksIfNeeded,
    private val setPlanStartTime: SetPlanStartTimeUseCase,
    private val setSelectedReadingPlan: SetSelectedReadingPlan,
    private val findFirstWeekWithUnreadBook: FindFirstWeekWithUnreadBook,
    private val weeksPlanPresentationMapper: WeeksPlanPresentationMapper,
    private val calculateIsFirstUnreadWeekVisible: CalculateIsFirstUnreadWeekVisible,
    private val deleteProgressMapper: DeleteProgressMapper,
    private val updateDayReadStatus: UpdateDayReadStatusUseCase,
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
            if (getPlanStartDate().first() == null) {
                setPlanStartTime(SetPlanStartTimeUseCase.Strategy.Now)
            }
            initializeBooksIfNeeded()
        }

        observe(calculateBibleProgress()) { progress ->
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
                currentPlansModel?.let { plansModel ->
                    val selectedWeeks = when (selectedPlan) {
                        ReadingPlanType.CHRONOLOGICAL -> plansModel.chronologicalOrder
                        ReadingPlanType.BOOKS -> plansModel.booksOrder
                    }
                    val weekPresentationModels = selectedWeeks.mapToPresentation()

                    ReadingPlanUiState.Loaded(
                        weekPlans = weekPresentationModels,
                        progress = currentBibleProgress,
                        selectedReadingPlan = selectedPlan,
                        isShowingMenu = currentState.isShowingMenu,
                        scrollToWeekNumber = currentState.scrollToWeekNumber,
                        scrollToTop = currentState.scrollToTop,
                        isScrolledDown = currentState.isScrolledDown,
                        isFirstUnreadWeekVisible = calculateIsFirstUnreadWeekVisible(
                            weekPlans = weekPresentationModels,
                            isScrolledDown = currentState.isScrolledDown,
                        ),
                    )
                } ?: when (currentState) {
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

                val weekPresentationModels = selectedWeeks.mapToPresentation()

                val isScrolledDown = currentState.isScrolledDown
                ReadingPlanUiState.Loaded(
                    weekPlans = weekPresentationModels,
                    progress = currentBibleProgress,
                    selectedReadingPlan = selectedPlan,
                    isShowingMenu = uiState.value.isShowingMenu,
                    scrollToWeekNumber = scrollToWeekNumber,
                    scrollToTop = currentState.scrollToTop,
                    isScrolledDown = isScrolledDown,
                    isFirstUnreadWeekVisible = calculateIsFirstUnreadWeekVisible(
                        weekPlans = weekPresentationModels,
                        isScrolledDown = isScrolledDown,
                    ),
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

                            val weekPresentationModels = currentUiState.weekPlans
                                .map { it.weekPlan }
                                .mapToPresentation()

                            currentUiState.copy(
                                weekPlans = weekPresentationModels,
                                scrollToWeekNumber = currentUiState.scrollToWeekNumber,
                                scrollToTop = currentUiState.scrollToTop,
                                isScrolledDown = currentUiState.isScrolledDown,
                                isFirstUnreadWeekVisible = calculateIsFirstUnreadWeekVisible(
                                    weekPlans = weekPresentationModels,
                                    isScrolledDown = currentUiState.isScrolledDown,
                                ),
                            )
                        }

                        is ReadingPlanUiState.Loading -> {
                            currentUiState
                        }
                    }
                }
            }

            is ReadingPlanUiEvent.OnDayReadClick -> {
                onDayReadClick(event)
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
                event.option.toUiAction()?.let(::emitUiAction)
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
                                val weekPresentationModels = state.weekPlans.map { it.weekPlan }.mapToPresentation()
                                state.copy(
                                    weekPlans = weekPresentationModels,
                                    scrollToWeekNumber = state.scrollToWeekNumber,
                                    scrollToTop = state.scrollToTop,
                                    isScrolledDown = state.isScrolledDown,
                                    isFirstUnreadWeekVisible = calculateIsFirstUnreadWeekVisible(
                                        weekPlans = weekPresentationModels,
                                        isScrolledDown = state.isScrolledDown,
                                    ),
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
                        is ReadingPlanUiState.Loaded -> {
                            state.copy(
                                isScrolledDown = event.isScrolledDown,
                                isFirstUnreadWeekVisible = calculateIsFirstUnreadWeekVisible(
                                    weekPlans = state.weekPlans,
                                    isScrolledDown = event.isScrolledDown,
                                ),
                            )
                        }

                        is ReadingPlanUiState.Loading -> {
                            state.copy(isScrolledDown = event.isScrolledDown)
                        }
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

            ReadingPlanUiEvent.OnEditPlanClick -> {
                emitUiAction(ReadingPlanUiAction.GoToChangeStartDate)
            }
        }
    }

    private fun onDayReadClick(event: ReadingPlanUiEvent.OnDayReadClick) {
        val currentUiState = _uiState.value

        if (currentUiState is ReadingPlanUiState.Loaded) {
            val safeCurrentPlans = currentPlansModel ?: return
            val week = safeCurrentPlans.let { plansModel ->
                val selectedWeeks = when (currentUiState.selectedReadingPlan) {
                    ReadingPlanType.CHRONOLOGICAL -> plansModel.chronologicalOrder
                    ReadingPlanType.BOOKS -> plansModel.booksOrder
                }
                selectedWeeks.find { it.number == event.weekNumber }
            }
            val day = week?.days?.find { it.number == event.dayNumber } ?: return
            val newReadStatus = !day.isRead
            viewModelScope.launch {
                updateDayReadStatus(
                    weekNumber = event.weekNumber,
                    dayNumber = event.dayNumber,
                    readingPlanType = currentUiState.selectedReadingPlan,
                    isRead = newReadStatus,
                )
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

    private fun OverflowOption.toUiAction(): ReadingPlanUiAction? = when (this) {
        OverflowOption.DELETE_PROGRESS -> deleteProgressMapper.map(uiState.value)
        OverflowOption.EDIT_START_DAY -> ReadingPlanUiAction.GoToChangeStartDate
    }

    private fun List<WeekPlanModel>.mapToPresentation(): List<WeekPlanPresentationModel> =
        weeksPlanPresentationMapper.map(
            weeks = this,
            expandedWeeks = expandedWeeks,
        )

    private fun emitUiAction(uiAction: ReadingPlanUiAction) {
        viewModelScope.launch {
            _uiAction.emit(uiAction)
        }
    }
}
