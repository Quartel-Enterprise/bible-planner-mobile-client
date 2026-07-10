package com.quare.bibleplanner.feature.readingplan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.usecase.CalculateBibleProgressUseCase
import com.quare.bibleplanner.core.loginnudge.domain.usecase.RequestLoginNudgeIfNeeded
import com.quare.bibleplanner.core.model.plan.PlansModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.UpdateDayReadStatusUseCase
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanDayRef
import com.quare.bibleplanner.feature.readingplan.domain.tracker.BibleProgressMilestoneTracker
import com.quare.bibleplanner.feature.readingplan.domain.tracker.ReadingStreakMilestoneTracker
import com.quare.bibleplanner.feature.readingplan.domain.usecase.FindFirstWeekWithUnreadBook
import com.quare.bibleplanner.feature.readingplan.domain.usecase.GetPlanMotivationMessage
import com.quare.bibleplanner.feature.readingplan.domain.usecase.GetSelectedReadingPlanFlow
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolvePlanStatus
import com.quare.bibleplanner.feature.readingplan.domain.usecase.SetSelectedReadingPlan
import com.quare.bibleplanner.feature.readingplan.presentation.factory.ReadingPlanStateFactory
import com.quare.bibleplanner.feature.readingplan.presentation.mapper.DeleteProgressMapper
import com.quare.bibleplanner.feature.readingplan.presentation.mapper.WeeksPlanPresentationMapper
import com.quare.bibleplanner.feature.readingplan.presentation.model.OverflowOption
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiAction
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekGroup
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
    calculateBibleProgress: CalculateBibleProgressUseCase,
    private val setSelectedReadingPlan: SetSelectedReadingPlan,
    private val findFirstWeekWithUnreadBook: FindFirstWeekWithUnreadBook,
    private val getPlanMotivationMessage: GetPlanMotivationMessage,
    private val resolvePlanStatus: ResolvePlanStatus,
    private val weeksPlanPresentationMapper: WeeksPlanPresentationMapper,
    private val deleteProgressMapper: DeleteProgressMapper,
    private val updateDayReadStatus: UpdateDayReadStatusUseCase,
    private val requestLoginNudgeIfNeeded: RequestLoginNudgeIfNeeded,
    private val trackEvent: TrackEvent,
    private val bibleProgressMilestoneTracker: BibleProgressMilestoneTracker,
    private val readingStreakMilestoneTracker: ReadingStreakMilestoneTracker,
) : ViewModel() {
    private val _uiState: MutableStateFlow<ReadingPlanUiState> = MutableStateFlow(factory.createFirstState())
    val uiState: StateFlow<ReadingPlanUiState> = _uiState

    private val _uiAction: MutableSharedFlow<ReadingPlanUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<ReadingPlanUiAction> = _uiAction

    private var currentPlansModel: PlansModel? = null
    private var currentBibleProgress: Float = 0f
    private val expandedWeeks = mutableSetOf<Int>()
    private val pendingReadOverrides = mutableMapOf<DayReadKey, Boolean>()
    private var lastAuthoritativeReadDayCount: Int? = null
    private var isFirstLoad = true

    init {
        observe(calculateBibleProgress()) { progress ->
            bibleProgressMilestoneTracker.onProgress(progress)
            currentBibleProgress = progress
            _uiState.update { currentState ->
                when (currentState) {
                    is ReadingPlanUiState.Loaded -> {
                        val rawWeeks = currentState.weekPlans.map { it.weekPlan }
                        currentState.copy(
                            progress = progress,
                            motivationMessage = getPlanMotivationMessage(rawWeeks, progress),
                        )
                    }

                    is ReadingPlanUiState.Loading -> currentState
                }
            }
        }
        observe(getSelectedReadingPlanFlow()) { selectedPlan ->
            _uiState.update { currentState ->
                val plansModel = currentPlansModel
                if (plansModel == null) {
                    when (currentState) {
                        is ReadingPlanUiState.Loaded -> currentState.copy(selectedReadingPlan = selectedPlan)
                        is ReadingPlanUiState.Loading -> currentState.copy(selectedReadingPlan = selectedPlan)
                    }
                } else {
                    buildLoadedState(
                        selectedPlan = selectedPlan,
                        selectedWeeks = plansModel.withReadOverrides().weeksFor(selectedPlan),
                        base = currentState,
                    )
                }
            }
        }
        observe(getPlansByWeek()) { plansModel ->
            reconcileReadOverrides(plansModel)
            currentPlansModel = plansModel
            val authoritativeReadDays = plansModel
                .weeksFor(_uiState.value.selectedReadingPlan)
                .sumOf { week -> week.days.count { it.isRead } }
            val previousReadDays = lastAuthoritativeReadDayCount
            val progressWasReset = previousReadDays != null && previousReadDays > 0 && authoritativeReadDays == 0
            lastAuthoritativeReadDayCount = authoritativeReadDays
            _uiState.update { currentState ->
                val selectedPlan = currentState.selectedReadingPlan
                val selectedWeeks = plansModel.withReadOverrides().weeksFor(selectedPlan)

                var scrollToWeekNumber = currentState.scrollToWeekNumber
                var scrollToWeekIsAutomatic = currentState.scrollToWeekIsAutomatic
                if (isFirstLoad) {
                    val firstUnreadWeekNumber = findFirstWeekWithUnreadBook(selectedWeeks)
                    if (firstUnreadWeekNumber != null) {
                        expandedWeeks.add(firstUnreadWeekNumber)
                        scrollToWeekNumber = firstUnreadWeekNumber
                        scrollToWeekIsAutomatic = true
                    }
                    isFirstLoad = false
                } else if (progressWasReset) {
                    expandedWeeks.clear()
                    findFirstWeekWithUnreadBook(selectedWeeks)?.let { expandedWeeks.add(it) }
                }

                buildLoadedState(
                    selectedPlan = selectedPlan,
                    selectedWeeks = selectedWeeks,
                    base = currentState,
                    scrollToWeekNumber = scrollToWeekNumber,
                    scrollToWeekIsAutomatic = scrollToWeekIsAutomatic,
                )
            }
        }
        observe(uiState) { state ->
            (state as? ReadingPlanUiState.Loaded)
                ?.planStatus
                ?.streakDays
                ?.let(readingStreakMilestoneTracker::onStreak)
        }
    }

    fun onEvent(event: ReadingPlanUiEvent) {
        when (event) {
            is ReadingPlanUiEvent.OnPlanClick -> {
                if (event.type != uiState.value.selectedReadingPlan) {
                    trackEvent(
                        name = AnalyticsEventNames.PLAN_SELECTED,
                        params = mapOf(AnalyticsParams.PLAN_TYPE to event.type.name.lowercase()),
                    )
                }
                viewModelScope.launch {
                    setSelectedReadingPlan(event.type)
                }
            }

            is ReadingPlanUiEvent.OnWeekExpandClick -> toggleWeekExpansion(event.weekNumber)

            is ReadingPlanUiEvent.OnDayReadClick -> onDayReadClick(event)

            is ReadingPlanUiEvent.OnDayClick -> {
                emitUiAction(
                    ReadingPlanUiAction.GoToDay(
                        dayNumber = event.dayNumber,
                        weekNumber = event.weekNumber,
                        readingPlanType = uiState.value.selectedReadingPlan,
                    ),
                )
            }

            ReadingPlanUiEvent.OnOverflowClick -> changeMenuVisibility(true)

            ReadingPlanUiEvent.OnOverflowDismiss -> changeMenuVisibility(false)

            is ReadingPlanUiEvent.OnOverflowOptionClick -> {
                changeMenuVisibility(false)
                event.option.toUiAction()?.let(::emitUiAction)
            }

            ReadingPlanUiEvent.OnToggleUpcomingExpanded -> {
                loadedState()?.let { state ->
                    val isExpanded = !state.upcomingExpanded
                    updateLoaded { it.copy(upcomingExpanded = isExpanded) }
                    trackGroupToggled(
                        group = GROUP_UPCOMING,
                        isExpanded = isExpanded,
                    )
                }
            }

            ReadingPlanUiEvent.OnToggleCompletedExpanded -> {
                loadedState()?.let { state ->
                    val isExpanded = !state.completedExpanded
                    updateLoaded { it.copy(completedExpanded = isExpanded) }
                    trackGroupToggled(
                        group = GROUP_COMPLETED,
                        isExpanded = isExpanded,
                    )
                }
            }

            ReadingPlanUiEvent.OnGoToActiveRowClick -> {
                trackShortcutUsed(SHORTCUT_ACTIVE_ROW)
                scrollAndFlashToDay(loadedState()?.planStatus?.nextDay)
            }

            ReadingPlanUiEvent.OnSkipToTodayClick -> {
                trackShortcutUsed(SHORTCUT_TODAY)
                scrollAndFlashToDay(loadedState()?.planStatus?.todayDay)
            }

            ReadingPlanUiEvent.OnScrollToTopClick -> {
                trackShortcutUsed(SHORTCUT_SCROLL_TOP)
                updateState { state ->
                    when (state) {
                        is ReadingPlanUiState.Loaded -> state.copy(scrollToTop = true)
                        is ReadingPlanUiState.Loading -> state.copy(scrollToTop = true)
                    }
                }
            }

            is ReadingPlanUiEvent.OnScrollStateChange -> {
                updateState { state ->
                    when (state) {
                        is ReadingPlanUiState.Loaded -> state.copy(isScrolledDown = event.isScrolledDown)
                        is ReadingPlanUiState.Loading -> state.copy(isScrolledDown = event.isScrolledDown)
                    }
                }
            }

            is ReadingPlanUiEvent.OnActiveRowVisibilityChange -> {
                updateState { state ->
                    when (state) {
                        is ReadingPlanUiState.Loaded -> state.copy(isActiveRowVisible = event.isActiveRowVisible)
                        is ReadingPlanUiState.Loading -> state.copy(isActiveRowVisible = event.isActiveRowVisible)
                    }
                }
            }

            ReadingPlanUiEvent.OnScrollToWeekCompleted -> {
                updateState { state ->
                    when (state) {
                        is ReadingPlanUiState.Loaded -> state.copy(
                            scrollToWeekNumber = 0,
                            scrollToWeekIsAutomatic = false,
                        )

                        is ReadingPlanUiState.Loading -> state.copy(
                            scrollToWeekNumber = 0,
                            scrollToWeekIsAutomatic = false,
                        )
                    }
                }
            }

            ReadingPlanUiEvent.OnScrollToTopCompleted -> {
                updateState { state ->
                    when (state) {
                        is ReadingPlanUiState.Loaded -> state.copy(scrollToTop = false)
                        is ReadingPlanUiState.Loading -> state.copy(scrollToTop = false)
                    }
                }
            }

            ReadingPlanUiEvent.OnFlashCompleted -> {
                updateLoaded { state ->
                    state.copy(flashTargetGlobalIndex = 0)
                }
            }

            ReadingPlanUiEvent.OnEditPlanClick -> emitUiAction(ReadingPlanUiAction.GoToChangeStartDate)
        }
    }

    private fun toggleWeekExpansion(weekNumber: Int) {
        if (expandedWeeks.contains(weekNumber)) {
            expandedWeeks.remove(weekNumber)
        } else {
            expandedWeeks.add(weekNumber)
        }
        updateLoaded { state ->
            state.copy(weekPlans = state.weekPlans.map { it.weekPlan }.mapToPresentation())
        }
        trackEvent(
            name = AnalyticsEventNames.PLAN_WEEK_TOGGLED,
            params = mapOf(
                AnalyticsParams.WEEK_NUMBER to weekNumber,
                AnalyticsParams.IS_EXPANDED to expandedWeeks.contains(weekNumber),
            ),
        )
    }

    private fun trackGroupToggled(
        group: String,
        isExpanded: Boolean,
    ) {
        trackEvent(
            name = AnalyticsEventNames.PLAN_GROUP_TOGGLED,
            params = mapOf(
                AnalyticsParams.GROUP to group,
                AnalyticsParams.IS_EXPANDED to isExpanded,
            ),
        )
    }

    private fun trackShortcutUsed(shortcut: String) {
        trackEvent(
            name = AnalyticsEventNames.PLAN_SHORTCUT_USED,
            params = mapOf(AnalyticsParams.SHORTCUT to shortcut),
        )
    }

    private fun scrollAndFlashToDay(dayRef: PlanDayRef?) {
        if (dayRef == null) return
        expandedWeeks.add(dayRef.weekNumber)
        updateLoaded { state ->
            val targetGroup = state.weekPlans
                .find { it.weekPlan.number == dayRef.weekNumber }
                ?.group
            state.copy(
                weekPlans = state.weekPlans.map { it.weekPlan }.mapToPresentation(),
                upcomingExpanded = state.upcomingExpanded || targetGroup == WeekGroup.Upcoming,
                completedExpanded = state.completedExpanded || targetGroup == WeekGroup.Completed,
                scrollToWeekNumber = dayRef.weekNumber,
                scrollToWeekIsAutomatic = false,
                flashTargetGlobalIndex = dayRef.globalIndex,
            )
        }
    }

    private fun onDayReadClick(event: ReadingPlanUiEvent.OnDayReadClick) {
        val currentUiState = _uiState.value
        if (currentUiState !is ReadingPlanUiState.Loaded) return
        val selectedPlan = currentUiState.selectedReadingPlan
        val day = currentUiState.weekPlans
            .find { it.weekPlan.number == event.weekNumber }
            ?.weekPlan
            ?.days
            ?.find { it.number == event.dayNumber }
            ?: return
        val newReadStatus = !day.isRead
        trackEvent(
            name = AnalyticsEventNames.DAY_READ_TOGGLED,
            params = mapOf(
                AnalyticsParams.PLAN_TYPE to selectedPlan.name.lowercase(),
                AnalyticsParams.WEEK_NUMBER to event.weekNumber,
                AnalyticsParams.DAY_NUMBER to event.dayNumber,
                AnalyticsParams.IS_READ to newReadStatus,
                AnalyticsParams.SOURCE to SOURCE_PLAN_LIST,
            ),
        )
        val currentWeekBefore = currentUiState.weekPlans
            .firstOrNull { it.group == WeekGroup.Current }
            ?.weekPlan
            ?.number

        pendingReadOverrides[DayReadKey(selectedPlan, event.weekNumber, event.dayNumber)] = newReadStatus
        focusCurrentWeekIfJustCompleted(
            completedWeekNumber = event.weekNumber,
            currentWeekBefore = currentWeekBefore,
            didMarkRead = newReadStatus,
            plan = selectedPlan,
        )
        rebuildLoadedFromCurrentModel()

        viewModelScope.launch {
            updateDayReadStatus(
                weekNumber = event.weekNumber,
                dayNumber = event.dayNumber,
                readingPlanType = selectedPlan,
                isRead = newReadStatus,
            )
            requestLoginNudgeIfNeeded()
        }
    }

    private fun focusCurrentWeekIfJustCompleted(
        completedWeekNumber: Int,
        currentWeekBefore: Int?,
        didMarkRead: Boolean,
        plan: ReadingPlanType,
    ) {
        if (!didMarkRead || completedWeekNumber != currentWeekBefore) return
        val effectiveWeeks = currentPlansModel?.withReadOverrides()?.weeksFor(plan).orEmpty()
        val isWeekComplete = effectiveWeeks
            .find { it.number == completedWeekNumber }
            ?.days
            ?.all { it.isRead } == true
        if (!isWeekComplete) return
        val newCurrentWeek = effectiveWeeks
            .firstOrNull { week -> week.days.any { !it.isRead } }
            ?.number
        expandedWeeks.clear()
        newCurrentWeek?.let { expandedWeeks.add(it) }
    }

    private fun rebuildLoadedFromCurrentModel() {
        val model = currentPlansModel ?: return
        updateLoaded { state ->
            buildLoadedState(
                selectedPlan = state.selectedReadingPlan,
                selectedWeeks = model.withReadOverrides().weeksFor(state.selectedReadingPlan),
                base = state,
            )
        }
    }

    private fun PlansModel.withReadOverrides(): PlansModel {
        if (pendingReadOverrides.isEmpty()) return this
        var chronological = chronologicalOrder
        var books = booksOrder
        pendingReadOverrides.forEach { (key, isRead) ->
            when (key.plan) {
                ReadingPlanType.CHRONOLOGICAL ->
                    chronological = chronological.applyReadOverride(key.weekNumber, key.dayNumber, isRead)

                ReadingPlanType.BOOKS -> books = books.applyReadOverride(key.weekNumber, key.dayNumber, isRead)
            }
        }
        return copy(chronologicalOrder = chronological, booksOrder = books)
    }

    private fun List<WeekPlanModel>.applyReadOverride(
        weekNumber: Int,
        dayNumber: Int,
        isRead: Boolean,
    ): List<WeekPlanModel> = map { week ->
        if (week.number != weekNumber) {
            week
        } else {
            week.copy(
                days = week.days.map { day ->
                    if (day.number != dayNumber) {
                        day
                    } else {
                        day.copy(
                            isRead = isRead,
                            passages = day.passages.map { it.copy(isRead = isRead) },
                        )
                    }
                },
            )
        }
    }

    private fun reconcileReadOverrides(authoritative: PlansModel) {
        pendingReadOverrides.entries.removeAll { (key, isRead) ->
            val day = authoritative
                .weeksFor(key.plan)
                .find { it.number == key.weekNumber }
                ?.days
                ?.find { it.number == key.dayNumber }
            day != null && day.isRead == isRead
        }
    }

    private fun changeMenuVisibility(isShowing: Boolean) {
        updateState { state ->
            when (state) {
                is ReadingPlanUiState.Loaded -> state.copy(isShowingMenu = isShowing)
                is ReadingPlanUiState.Loading -> state.copy(isShowingMenu = isShowing)
            }
        }
    }

    private fun buildLoadedState(
        selectedPlan: ReadingPlanType,
        selectedWeeks: List<WeekPlanModel>,
        base: ReadingPlanUiState,
        scrollToWeekNumber: Int = base.scrollToWeekNumber,
        scrollToWeekIsAutomatic: Boolean = base.scrollToWeekIsAutomatic,
    ): ReadingPlanUiState.Loaded {
        val loadedBase = base as? ReadingPlanUiState.Loaded
        return ReadingPlanUiState.Loaded(
            weekPlans = selectedWeeks.mapToPresentation(),
            progress = currentBibleProgress,
            motivationMessage = getPlanMotivationMessage(selectedWeeks, currentBibleProgress),
            planStatus = resolvePlanStatus(selectedWeeks),
            upcomingExpanded = loadedBase?.upcomingExpanded ?: false,
            completedExpanded = loadedBase?.completedExpanded ?: false,
            flashTargetGlobalIndex = loadedBase?.flashTargetGlobalIndex ?: 0,
            selectedReadingPlan = selectedPlan,
            isShowingMenu = base.isShowingMenu,
            scrollToWeekNumber = scrollToWeekNumber,
            scrollToWeekIsAutomatic = scrollToWeekIsAutomatic,
            scrollToTop = base.scrollToTop,
            isScrolledDown = base.isScrolledDown,
            isActiveRowVisible = base.isActiveRowVisible,
        )
    }

    private fun loadedState(): ReadingPlanUiState.Loaded? = _uiState.value as? ReadingPlanUiState.Loaded

    private fun updateLoaded(transform: (ReadingPlanUiState.Loaded) -> ReadingPlanUiState.Loaded) {
        _uiState.update { state ->
            when (state) {
                is ReadingPlanUiState.Loaded -> transform(state)
                is ReadingPlanUiState.Loading -> state
            }
        }
    }

    private fun updateState(transform: (ReadingPlanUiState) -> ReadingPlanUiState) {
        _uiState.update(transform)
    }

    private fun PlansModel.weeksFor(plan: ReadingPlanType): List<WeekPlanModel> = when (plan) {
        ReadingPlanType.CHRONOLOGICAL -> chronologicalOrder
        ReadingPlanType.BOOKS -> booksOrder
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

    private data class DayReadKey(
        val plan: ReadingPlanType,
        val weekNumber: Int,
        val dayNumber: Int,
    )

    private companion object {
        const val SOURCE_PLAN_LIST = "plan_list"
        const val GROUP_UPCOMING = "upcoming"
        const val GROUP_COMPLETED = "completed"
        const val SHORTCUT_ACTIVE_ROW = "active_row"
        const val SHORTCUT_TODAY = "today"
        const val SHORTCUT_SCROLL_TOP = "scroll_top"
    }
}
