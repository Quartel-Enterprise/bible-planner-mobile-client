package com.quare.bibleplanner.feature.day.screenshots

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.feature.day.presentation.model.DatePickerUiState
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.ui.component.date.DatePresentationModel
import com.quare.bibleplanner.ui.utils.toStringResource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

private const val DAY_IN_WEEK = 4
private const val WEEK_NUMBER = 16
private const val TOTAL_VERSES = 96

/**
 * A note of the kind a reader actually writes, one per language: the screen ships with an empty
 * field and a placeholder, which shows the feature exists but not what it is for.
 */
private val notesByLocale = mapOf(
    "en-US" to "The queen of Sheba travelled for months to hear this wisdom. Mine sits on the " +
        "bedside table.",
    "pt-BR" to "A rainha de Sabá viajou meses para ouvir essa sabedoria. A minha fica na mesa de " +
        "cabeceira.",
    "es" to "La reina de Sabá viajó meses para oír esta sabiduría. La mía está en la mesita de " +
        "noche.",
)

/** The same day the plan screenshot calls today: 1 Kings 10-12, every chapter ticked off. */
private val chapters = listOf(10, 11, 12)

private val readDate = LocalDate(year = 2026, monthNumber = 7, dayOfMonth = 23)

private fun passage() = PassageModel(
    bookId = BookId.FIRST_KI,
    chapters = chapters.map { chapter ->
        ChapterModel(
            number = chapter,
            startVerse = null,
            endVerse = null,
            bookId = BookId.FIRST_KI,
        )
    },
    isRead = false,
    chapterRanges = "${chapters.first()}-${chapters.last()}",
)

@OptIn(ExperimentalMaterial3Api::class)
private fun datePickerUiState() = DatePickerUiState(
    visiblePicker = null,
    selectedDateMillis = null,
    selectedLocalDate = readDate,
    selectableDates = object : SelectableDates {},
    initialTimestamp = 0L,
    initialHour = 7,
    initialMinute = 12,
)

internal fun dayUiState(locale: String): DayUiState.Loaded = DayUiState.Loaded(
    day = DayModel(
        number = DAY_IN_WEEK,
        passages = listOf(passage()),
        isRead = false,
        totalVerses = TOTAL_VERSES,
        readVerses = TOTAL_VERSES,
        readTimestamp = null,
        plannedReadDate = readDate,
        notes = notesByLocale.getValue(locale),
        isToday = true,
    ),
    weekNumber = WEEK_NUMBER,
    dayRoute = DayNavRoute(
        dayNumber = DAY_IN_WEEK,
        weekNumber = WEEK_NUMBER,
        readingPlanType = "CHRONOLOGICAL",
    ),
    datePickerUiState = datePickerUiState(),
    formattedReadDate = DatePresentationModel(
        day = readDate.day,
        month = Month.JULY.toStringResource(),
        year = readDate.year,
        minute = "12",
        hour = "07",
    ),
    chapterReadStatus = chapters.indices.associate { index -> (0 to index) to true },
    // The app counts chapters here, not PassageModel objects — see calculatePassageCounts in
    // DayUiStateFlowFactory — so both numbers are derived instead of written by hand.
    completedPassagesCount = chapters.size,
    totalPassagesCount = chapters.size,
)
