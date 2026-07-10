package com.quare.bibleplanner.feature.day.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.failed_to_toggle_chapter_message
import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.loginnudge.domain.usecase.RequestLoginNudgeIfNeeded
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.feature.day.domain.model.ChapterClickStrategy
import com.quare.bibleplanner.feature.day.domain.model.DayUseCases
import com.quare.bibleplanner.feature.day.domain.model.UpdateReadStatusOfPassageStrategy
import com.quare.bibleplanner.feature.day.presentation.factory.DayUiStateFlowFactory
import com.quare.bibleplanner.feature.day.presentation.mapper.DeleteRouteNotesMapper
import com.quare.bibleplanner.feature.day.presentation.model.DatePickerUiState
import com.quare.bibleplanner.feature.day.presentation.model.DayUiAction
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.feature.day.presentation.model.PickerType
import com.quare.bibleplanner.ui.utils.observe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class DayViewModel(
    route: DayNavRoute,
    private val useCases: DayUseCases,
    private val dayUiStateFlowFactory: DayUiStateFlowFactory,
    private val deleteRouteNotesMapper: DeleteRouteNotesMapper,
    private val requestLoginNudgeIfNeeded: RequestLoginNudgeIfNeeded,
    private val applicationScope: ApplicationScope,
    private val trackEvent: TrackEvent,
    val platform: Platform,
) : ViewModel() {
    private val _uiState: MutableStateFlow<DayUiState> = MutableStateFlow(DayUiState.Loading)
    val uiState: StateFlow<DayUiState> = _uiState.asStateFlow()
    private val safeLoadedState: DayUiState.Loaded? get() = uiState.value as? DayUiState.Loaded

    private val _uiAction: MutableSharedFlow<DayUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<DayUiAction> = _uiAction
    private val weekNumber = route.weekNumber
    private val dayNumber = route.dayNumber
    private val readingPlanType = ReadingPlanType.valueOf(route.readingPlanType)

    private var notesSaveJob: Job? = null
    private val notesDebounceDelay: Duration = 2.seconds

    init {
        loadDayDetails()
    }

    private fun loadDayDetails() {
        observe(
            dayUiStateFlowFactory.createUiState(
                weekNumber = weekNumber,
                dayNumber = dayNumber,
                readingPlanType = readingPlanType,
                currentState = _uiState.value as? DayUiState.Loaded,
            ),
        ) { state ->
            _uiState.update { state }
        }
    }

    private fun updateDatePickerState(transform: (DatePickerUiState) -> DatePickerUiState) {
        updateLoadedState { loaded ->
            loaded.copy(
                datePickerUiState = transform(loaded.datePickerUiState),
            )
        }
    }

    fun onEvent(event: DayUiEvent) {
        when (event) {
            is DayUiEvent.OnChapterCheckboxClick -> {
                onChapterToggle(event)
            }

            DayUiEvent.OnDayReadToggle -> {
                onDayReadToggle()
            }

            is DayUiEvent.OnEditReadDate -> {
                onEditReadDate(event)
            }

            is DayUiEvent.OnEditDateClick -> {
                onEditDateClick()
            }

            is DayUiEvent.OnShowTimePicker -> {
                updateDatePickerState { it.copy(visiblePicker = PickerType.TIME) }
            }

            is DayUiEvent.OnDismissPicker -> {
                updateDatePickerState { it.copy(visiblePicker = null) }
            }

            is DayUiEvent.OnDateSelected -> {
                onDateSelected(event)
            }

            is DayUiEvent.OnNotesChanged -> {
                onNotesChanged(event)
            }

            is DayUiEvent.OnNotesClear -> {
                onNotesClear()
            }

            is DayUiEvent.OnNotesFocus -> {
                onNotesFocus()
            }

            is DayUiEvent.OnBackClick -> {
                backToPreviousScreen()
            }

            is DayUiEvent.OnChapterClick -> {
                onChapterClick(event.strategy)
            }

            is DayUiEvent.OnDayStudySubscribeClick -> {
                navigateToPaywall()
            }

            is DayUiEvent.OnDayStudyLoginRequired -> {
                navigateToDayStudyLoginWarning()
            }

            is DayUiEvent.OnDayStudyMessage -> {
                showSnackBarText(event.message)
            }
        }
    }

    private fun navigateToPaywall() {
        trackEvent(
            name = AnalyticsEventNames.PAYWALL_VIEWED,
            params = mapOf(AnalyticsParams.SOURCE to SOURCE_DAY_STUDY),
        )
        viewModelScope.launch {
            _uiAction.emit(DayUiAction.NavigateToRoute(PaywallNavRoute))
        }
    }

    private fun navigateToDayStudyLoginWarning() {
        viewModelScope.launch {
            _uiAction.emit(DayUiAction.NavigateToRoute(LoginWarningNavRoute(LoginWarningReason.DayStudy.key)))
        }
    }

    private fun showSnackBarText(message: String) {
        viewModelScope.launch {
            _uiAction.emit(DayUiAction.ShowSnackBarText(message))
        }
    }

    private fun onChapterClick(strategy: ChapterClickStrategy) {
        val chapterNumber = when (strategy) {
            is ChapterClickStrategy.NavigateToChapter -> strategy.chapterNumber
            is ChapterClickStrategy.NavigateToFirstChapterOfTheBook -> 1
        }
        viewModelScope.launch {
            _uiAction.emit(
                DayUiAction.NavigateToRoute(
                    ReadNavRoute(
                        bookId = strategy.bookId.name,
                        chapterNumber = chapterNumber,
                        isChapterRead = strategy.isChapterRead,
                        isFromBookDetails = false,
                    ),
                ),
            )
        }
    }

    private fun onDateSelected(event: DayUiEvent.OnDateSelected) {
        val utcDateMillis = event.utcDateMillis
        updateDatePickerState {
            it.copy(
                selectedDateMillis = utcDateMillis,
                selectedLocalDate = useCases.convertUtcDateToLocalDate(utcDateMillis),
                visiblePicker = PickerType.TIME,
            )
        }
    }

    private fun onEditDateClick() {
        updateDatePickerState { it.copy(visiblePicker = PickerType.DATE) }
    }

    private fun onEditReadDate(event: DayUiEvent.OnEditReadDate) {
        val currentState = _uiState.value as? DayUiState.Loaded ?: return
        val selectedLocalDate = currentState.datePickerUiState.selectedLocalDate ?: return
        val eventDuration = event.toDuration()

        updateDatePickerState {
            it.copy(
                visiblePicker = null,
                selectedDateMillis = null,
                selectedLocalDate = null,
            )
        }

        trackEvent(
            name = AnalyticsEventNames.READ_DATE_EDITED,
            params = dayParams(),
        )

        viewModelScope.launch {
            useCases.updateDayReadTimestampWithDateAndTime(
                weekNumber = weekNumber,
                dayNumber = dayNumber,
                readingPlanType = readingPlanType,
                selectedLocalDate = selectedLocalDate,
                eventDuration = eventDuration,
            )
        }
    }

    private fun onDayReadToggle() {
        val currentState = _uiState.value as? DayUiState.Loaded ?: return
        val newReadStatus = !currentState.day.isRead

        trackEvent(
            name = AnalyticsEventNames.DAY_READ_TOGGLED,
            params = dayParams() + mapOf(
                AnalyticsParams.IS_READ to newReadStatus,
                AnalyticsParams.SOURCE to SOURCE_DAY_SCREEN,
            ),
        )

        viewModelScope.launch {
            useCases.updateDayReadStatus(
                weekNumber = weekNumber,
                dayNumber = dayNumber,
                isRead = newReadStatus,
                readingPlanType = readingPlanType,
            )
            requestLoginNudgeIfNeeded()
        }
    }

    private fun onChapterToggle(event: DayUiEvent.OnChapterCheckboxClick) {
        val currentState = _uiState.value as? DayUiState.Loaded ?: return
        val passage = currentState.day.passages.getOrNull(event.strategy.passageIndex) ?: return

        viewModelScope.launch {
            useCases
                .toggleChapterReadStatus(
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                    strategy = event.strategy,
                    passage = passage,
                    readingPlanType = readingPlanType,
                ).onSuccess { isRead ->
                    trackChapterToggle(
                        strategy = event.strategy,
                        passage = passage,
                        isRead = isRead,
                    )
                }.onFailure {
                    _uiAction.emit(DayUiAction.ShowSnackBar(Res.string.failed_to_toggle_chapter_message))
                }
        }
    }

    private fun trackChapterToggle(
        strategy: UpdateReadStatusOfPassageStrategy,
        passage: PassageModel,
        isRead: Boolean,
    ) {
        when (strategy) {
            is UpdateReadStatusOfPassageStrategy.EntireBook -> {
                trackEvent(
                    name = AnalyticsEventNames.BOOK_READ_TOGGLED,
                    params = mapOf(
                        AnalyticsParams.BOOK_ID to passage.bookId.name.lowercase(),
                        AnalyticsParams.IS_READ to isRead,
                    ),
                )
            }

            is UpdateReadStatusOfPassageStrategy.Chapter -> {
                val chapterNumber = passage.chapters.getOrNull(strategy.chapterIndex)?.number ?: return
                trackEvent(
                    name = AnalyticsEventNames.CHAPTER_READ_TOGGLED,
                    params = dayParams() + mapOf(
                        AnalyticsParams.BOOK_ID to passage.bookId.name.lowercase(),
                        AnalyticsParams.CHAPTER_NUMBER to chapterNumber,
                        AnalyticsParams.IS_READ to isRead,
                        AnalyticsParams.SOURCE to SOURCE_DAY_SCREEN,
                    ),
                )
            }
        }
    }

    private fun DayUiEvent.OnEditReadDate.toDuration(): Duration = (hour * 60 + minute).minutes

    private fun onNotesChanged(event: DayUiEvent.OnNotesChanged) {
        updateLoadedState { loaded ->
            loaded.copy(day = loaded.day.copy(notes = event.notes))
        }
        notesSaveJob?.cancel()
        notesSaveJob = event.toJob()
    }

    private fun DayUiEvent.OnNotesChanged.toJob(): Job = viewModelScope.launch {
        delay(notesDebounceDelay)
        saveNotes(notes)
    }

    override fun onCleared() {
        super.onCleared()
        if (notesSaveJob?.isActive != true) return
        notesSaveJob?.cancel()
        val pendingNotes = safeLoadedState?.day?.notes
        applicationScope.launch { saveNotes(pendingNotes) }
    }

    private suspend fun saveNotes(notes: String?) {
        val sanitizedNotes = notes?.ifBlank { null }
        useCases.updateDayNotes(
            weekNumber = weekNumber,
            dayNumber = dayNumber,
            readingPlanType = readingPlanType,
            notes = sanitizedNotes,
        )
        sanitizedNotes?.let { savedNotes ->
            trackEvent(
                name = AnalyticsEventNames.NOTE_SAVED,
                params = dayParams() + mapOf(AnalyticsParams.NOTE_LENGTH to savedNotes.length),
            )
        }
    }

    private fun onNotesClear() {
        val loadedState = safeLoadedState ?: return
        viewModelScope.launch {
            _uiAction.emit(
                deleteRouteNotesMapper.map(
                    hasNotes = loadedState.hasNotes(),
                    readingPlanType = readingPlanType,
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                ),
            )
        }
    }

    private fun onNotesFocus() {
        val loadedState = safeLoadedState ?: return
        if (loadedState.hasNotes()) return

        viewModelScope.launch {
            if (useCases.shouldBlockAddNotes()) {
                // Ensure there are not notes in the ui due to a fast typing before the verification happens
                deleteNotesAsyncDueToBlockedAddNotes()
                blockAddNotes()
            }
        }
    }

    private fun CoroutineScope.deleteNotesAsyncDueToBlockedAddNotes() {
        launch {
            delay(500.milliseconds)
            val loadedState = safeLoadedState ?: return@launch
            if (!loadedState.hasNotes()) return@launch
            useCases.deleteDayNotes(
                weekNumber = weekNumber,
                dayNumber = dayNumber,
                readingPlanType = readingPlanType,
            )
        }
    }

    private suspend fun blockAddNotes() {
        val maxFreeNotes = useCases.getMaxFreeNotesAmount()
        trackEvent(
            name = AnalyticsEventNames.NOTES_LIMIT_REACHED,
            params = mapOf(AnalyticsParams.MAX_FREE_NOTES to maxFreeNotes),
        )
        _uiAction.run {
            emit(DayUiAction.ClearFocus)
            emit(DayUiAction.NavigateToRoute(AddNotesFreeWarningNavRoute(maxFreeNotes)))
        }
    }

    private fun updateLoadedState(transform: (DayUiState.Loaded) -> DayUiState.Loaded) {
        _uiState.update { currentState ->
            if (currentState is DayUiState.Loaded) {
                transform(currentState)
            } else {
                currentState
            }
        }
    }

    private fun DayUiState.Loaded.hasNotes(): Boolean = !day.notes.isNullOrEmpty()

    private fun backToPreviousScreen() {
        viewModelScope.launch {
            _uiAction.emit(DayUiAction.NavigateBack)
        }
    }

    private fun dayParams(): Map<String, Any> = mapOf(
        AnalyticsParams.PLAN_TYPE to readingPlanType.name.lowercase(),
        AnalyticsParams.WEEK_NUMBER to weekNumber,
        AnalyticsParams.DAY_NUMBER to dayNumber,
    )

    private companion object {
        const val SOURCE_DAY_SCREEN = "day_screen"
        const val SOURCE_DAY_STUDY = "day_study"
    }
}
