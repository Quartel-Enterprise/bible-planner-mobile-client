package com.quare.bibleplanner.feature.day.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute
import com.quare.bibleplanner.feature.day.domain.usecase.DayUseCases
import com.quare.bibleplanner.feature.day.presentation.factory.DayUiStateFlowFactory
import com.quare.bibleplanner.feature.day.presentation.mapper.DeleteRouteNotesMapper
import com.quare.bibleplanner.feature.day.presentation.model.DatePickerUiState
import com.quare.bibleplanner.feature.day.presentation.model.DayUiAction
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.feature.day.presentation.model.PickerType
import com.quare.bibleplanner.ui.utils.observe
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
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class DayViewModel(
    savedStateHandle: SavedStateHandle,
    private val useCases: DayUseCases,
    private val dayUiStateFlowFactory: DayUiStateFlowFactory,
    private val deleteRouteNotesMapper: DeleteRouteNotesMapper,
) : ViewModel() {
    private val _uiState: MutableStateFlow<DayUiState> = MutableStateFlow(DayUiState.Loading)
    val uiState: StateFlow<DayUiState> = _uiState.asStateFlow()

    private val _uiAction: MutableSharedFlow<DayUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<DayUiAction> = _uiAction
    private val route = savedStateHandle.toRoute<DayNavRoute>()
    private val weekNumber = route.weekNumber
    private val dayNumber = route.dayNumber
    private val readingPlanType = ReadingPlanType.valueOf(route.readingPlanType)

    private var notesSaveJob: Job? = null
    private val notesDebounceDelay: Duration = 500.milliseconds

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
            is DayUiEvent.OnChapterToggle -> {
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

        viewModelScope.launch {
            useCases.readDayToggleOperation(
                weekNumber = weekNumber,
                dayNumber = dayNumber,
                newReadStatus = newReadStatus,
                selectedReadingPlan = readingPlanType,
            )
        }
    }

    private fun onChapterToggle(event: DayUiEvent.OnChapterToggle) {
        val currentState = _uiState.value as? DayUiState.Loaded ?: return
        val passageIndex = event.passageIndex
        val chapterIndex = event.chapterIndex

        viewModelScope.launch {
            useCases.toggleChapterReadStatus(
                weekNumber = weekNumber,
                dayNumber = dayNumber,
                passageIndex = passageIndex,
                chapterIndex = chapterIndex,
                passage = currentState.day.passages.getOrNull(passageIndex) ?: return@launch,
                books = currentState.books,
                readingPlanType = readingPlanType,
            )
        }
    }

    private fun DayUiEvent.OnEditReadDate.toDuration(): Duration = (hour * 60 + minute).minutes

    private fun updateLoadedState(transform: (DayUiState.Loaded) -> DayUiState.Loaded) {
        _uiState.update { currentState ->
            if (currentState is DayUiState.Loaded) {
                transform(currentState)
            } else {
                currentState
            }
        }
    }

    private fun onNotesChanged(event: DayUiEvent.OnNotesChanged) {
        // Update local state immediately for responsive UI
        updateLoadedState { loaded ->
            loaded.copy(notesText = event.notes)
        }

        // Cancel previous save job if it exists
        notesSaveJob?.cancel()

        // Debounce database save to avoid excessive writes while user is typing
        notesSaveJob = viewModelScope.launch {
            delay(notesDebounceDelay)
            useCases.updateDayNotes(
                weekNumber = weekNumber,
                dayNumber = dayNumber,
                readingPlanType = readingPlanType,
                notes = event.notes.ifBlank { null },
            )
        }
    }

    private fun onNotesClear() {
        val currentState = _uiState.value as? DayUiState.Loaded ?: return
        viewModelScope.launch {
            _uiAction.emit(
                deleteRouteNotesMapper.map(
                    hasNotes = currentState.notesText.isNotEmpty(),
                    readingPlanType = readingPlanType,
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                ),
            )
        }
    }

    private fun onNotesFocus() {
        viewModelScope.launch {
            val currentState = _uiState.value as? DayUiState.Loaded ?: return@launch

            // If current day already has notes, allow focus
            if (currentState.notesText.isNotEmpty()) {
                return@launch
            }

            // Check if user has 3 or more days with notes
            val daysWithNotesCount = useCases.getDaysWithNotesCount()
            // TODO: Check if user is premium - for now, assume all users are free
            val isPremium = false

            if (daysWithNotesCount >= 3 && !isPremium) {
                // Clear focus before navigating to warning dialog
                updateLoadedState { loaded ->
                    loaded.copy(shouldClearNotesFocus = true)
                }
                // Reset the flag after a brief moment to allow re-triggering if needed
                viewModelScope.launch {
                    delay(100)
                    updateLoadedState { loaded ->
                        loaded.copy(shouldClearNotesFocus = false)
                    }
                }
                // Navigate to warning dialog
                _uiAction.emit(DayUiAction.NavigateToRoute(AddNotesFreeWarningNavRoute))
            }
        }
    }

    private fun backToPreviousScreen() {
        viewModelScope.launch {
            _uiAction.emit(DayUiAction.NavigateBack)
        }
    }
}
