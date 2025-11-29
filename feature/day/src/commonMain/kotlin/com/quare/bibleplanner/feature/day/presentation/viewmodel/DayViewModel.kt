package com.quare.bibleplanner.feature.day.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.feature.day.domain.usecase.DayUseCases
import com.quare.bibleplanner.feature.day.presentation.mapper.ReadDateFormatter
import androidx.compose.material3.SelectableDates
import com.quare.bibleplanner.feature.day.presentation.model.DatePickerUiState
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.feature.day.presentation.model.PickerType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalTime::class)
internal class DayViewModel(
    savedStateHandle: SavedStateHandle,
    private val useCases: DayUseCases,
    private val readDateFormatter: ReadDateFormatter,
) : ViewModel() {
    private val _uiState: MutableStateFlow<DayUiState> = MutableStateFlow(DayUiState.Loading)
    val uiState: StateFlow<DayUiState> = _uiState.asStateFlow()

    private val _backUiAction: MutableSharedFlow<Unit> = MutableSharedFlow()
    val backUiAction: SharedFlow<Unit> = _backUiAction

    private val dayNumber: Int
    private val weekNumber: Int
    private val readingPlanType: ReadingPlanType

    init {
        // Extract day and week numbers from savedStateHandle using toRoute extension
        val route = savedStateHandle.toRoute<DayNavRoute>()
        dayNumber = route.dayNumber
        weekNumber = route.weekNumber
        readingPlanType = ReadingPlanType.valueOf(route.readingPlanType)

        loadDayDetails()
    }

    private fun loadDayDetails() {
        combine(
            useCases.getDayDetails(weekNumber, dayNumber, readingPlanType),
            useCases.getBooks(),
        ) { day, books ->
            if (day != null) {
                val currentState = _uiState.value as? DayUiState.Loaded
                val existingDatePickerUiState = currentState?.datePickerUiState
                
                // Calculate initial timestamp and date components
                val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
                val initialTimestamp = day.readTimestamp ?: currentTimeMillis
                val initialDate = Instant.fromEpochMilliseconds(initialTimestamp)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                
                val datePickerUiState = if (existingDatePickerUiState?.selectableDates != null) {
                    existingDatePickerUiState.copy(
                        initialTimestamp = initialTimestamp,
                        initialHour = initialDate.hour,
                        initialMinute = initialDate.minute,
                    )
                } else {
                    (existingDatePickerUiState ?: DatePickerUiState(
                        visiblePicker = null,
                        selectedDateMillis = null,
                        selectedLocalDate = null,
                        selectableDates = createSelectableDates(),
                        initialTimestamp = initialTimestamp,
                        initialHour = initialDate.hour,
                        initialMinute = initialDate.minute,
                    )).copy(
                        selectableDates = createSelectableDates(),
                        initialTimestamp = initialTimestamp,
                        initialHour = initialDate.hour,
                        initialMinute = initialDate.minute,
                    )
                }
                
                DayUiState.Loaded(
                    day = day,
                    weekNumber = weekNumber,
                    books = books,
                    datePickerUiState = datePickerUiState,
                    formattedReadDate = day.readTimestamp?.let { readDateFormatter.format(it) },
                )
            } else {
                DayUiState.Loading
            }
        }.onEach { state ->
            _uiState.update { state }
        }.catch { error ->
            error.printStackTrace()
            _uiState.update { DayUiState.Loading }
        }.launchIn(viewModelScope)
    }

    @OptIn(ExperimentalTime::class)
    private fun createSelectableDates(): SelectableDates {
        return object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Block future dates - only allow dates up to now
                val now = Clock.System.now().toEpochMilliseconds()
                return utcTimeMillis <= now
            }

            override fun isSelectableYear(year: Int): Boolean {
                // Get current year from current timestamp
                val now = Clock.System.now().toEpochMilliseconds()
                val currentYear = Instant.fromEpochMilliseconds(now)
                    .toLocalDateTime(TimeZone.UTC)
                    .year
                return year <= currentYear
            }
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

    private fun updateDatePickerState(transform: (DatePickerUiState) -> DatePickerUiState) {
        updateLoadedState { loaded ->
            loaded.copy(
                datePickerUiState = transform(loaded.datePickerUiState),
            )
        }
    }


    fun onEvent(event: DayUiEvent) {
        when (event) {
            is DayUiEvent.OnChapterToggle -> onChapterToggle(event)

            is DayUiEvent.OnDayReadToggle -> onDayReadToggle(event)

            is DayUiEvent.OnEditReadDate -> onEditReadDate(event)

            is DayUiEvent.OnEditDateClick -> onEditDateClick()

            is DayUiEvent.OnShowTimePicker -> {
                updateDatePickerState { it.copy(visiblePicker = PickerType.TIME) }
            }

            is DayUiEvent.OnDismissPicker -> {
                updateDatePickerState { it.copy(visiblePicker = null) }
            }

            is DayUiEvent.OnDateSelected -> {
                // Convert dateMillis to LocalDate in the ViewModel
                @OptIn(ExperimentalTime::class)
                val dateTime = Instant.fromEpochMilliseconds(event.dateMillis)
                    .toLocalDateTime(TimeZone.UTC)
                val localDate = LocalDate(
                    year = dateTime.year,
                    month = dateTime.month,
                    dayOfMonth = dateTime.dayOfMonth,
                )
                updateDatePickerState {
                    it.copy(
                        selectedDateMillis = event.dateMillis,
                        selectedLocalDate = localDate,
                        visiblePicker = PickerType.TIME,
                    )
                }
            }

            is DayUiEvent.OnBackClick -> {
                viewModelScope.launch {
                    _backUiAction.emit(Unit)
                }
            }
        }
    }

    private fun onEditDateClick() {
        updateDatePickerState { it.copy(visiblePicker = PickerType.DATE) }
    }

    private fun onEditReadDate(event: DayUiEvent.OnEditReadDate) {
        val currentState = _uiState.value as? DayUiState.Loaded ?: return
        val selectedLocalDate = currentState.datePickerUiState.selectedLocalDate ?: return

        viewModelScope.launch {
            // Combine date and time using the local date directly (not converted from UTC)
            val timeZone = TimeZone.currentSystemDefault()
            val startOfDay = selectedLocalDate.atStartOfDayIn(timeZone)
            val timeOffsetMillis = (
                event.hour * 3600_000L +
                    event.minute * 60_000L
                )
            val duration = timeOffsetMillis.milliseconds
            val finalInstant = startOfDay + duration
            val finalTimestamp = finalInstant.toEpochMilliseconds()

            useCases.updateDayReadTimestamp(
                weekNumber = weekNumber,
                dayNumber = dayNumber,
                readTimestamp = finalTimestamp,
            )
            // Reset date picker state and update initial values
            val initialDate = Instant.fromEpochMilliseconds(finalTimestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            updateLoadedState { loaded ->
                loaded.copy(
                    datePickerUiState = DatePickerUiState(
                        visiblePicker = null,
                        selectedDateMillis = null,
                        selectedLocalDate = null,
                        selectableDates = createSelectableDates(),
                        initialTimestamp = finalTimestamp,
                        initialHour = initialDate.hour,
                        initialMinute = initialDate.minute,
                    ),
                    formattedReadDate = readDateFormatter.format(finalTimestamp),
                )
            }
        }
    }

    private fun onDayReadToggle(event: DayUiEvent.OnDayReadToggle) {
        viewModelScope.launch {
            useCases.updateDayReadStatus(
                weekNumber = weekNumber,
                dayNumber = dayNumber,
                isRead = event.isRead,
                readingPlanType = readingPlanType,
            )
            // State will be updated by the flow
        }
    }

    private fun onChapterToggle(event: DayUiEvent.OnChapterToggle) {
        val currentState = _uiState.value as? DayUiState.Loaded ?: return
        val passage = currentState.day.passages.getOrNull(event.passageIndex) ?: return

        // Determine the new read status for this chapter
        val newReadStatus = if (event.chapterIndex == -1) {
            // Entire book (no chapters)
            !passage.isRead
        } else {
            // Specific chapter - check if it's currently read
            if (event.chapterIndex < 0 || event.chapterIndex >= passage.chapters.size) return
            val chapter = passage.chapters[event.chapterIndex]
            val book = currentState.books.find { it.id == passage.bookId } ?: return
            val bookChapter = book.chapters.find { it.number == chapter.number } ?: return

            // Check if chapter is read based on verse ranges
            val startVerse = chapter.startVerse
            val endVerse = chapter.endVerse
            val isCurrentlyRead = when {
                startVerse != null && endVerse != null -> {
                    val requiredVerses = startVerse..endVerse
                    requiredVerses.all { verseNumber ->
                        bookChapter.verses.find { it.number == verseNumber }?.isRead == true
                    }
                }

                startVerse != null -> {
                    bookChapter.verses
                        .filter { it.number >= startVerse }
                        .all { it.isRead }
                }

                else -> {
                    bookChapter.isRead
                }
            }
            !isCurrentlyRead
        }

        viewModelScope.launch {
            useCases.updateChapterReadStatus(
                weekNumber = weekNumber,
                dayNumber = dayNumber,
                passageIndex = event.passageIndex,
                chapterIndex = event.chapterIndex,
                isRead = newReadStatus,
                readingPlanType = readingPlanType,
            )
            // State will be updated by the flow
        }
    }
}
