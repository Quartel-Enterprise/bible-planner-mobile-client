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

/**
 * The day the screenshots open on: Genesis 1-3, every chapter ticked off, carrying a note of the
 * kind a reader actually writes, one per language — the screen ships with an empty field and a
 * placeholder, which shows the feature exists but not what it is for.
 */
private const val DAY_IN_WEEK = 1
private const val WEEK_NUMBER = 1
private const val TOTAL_VERSES = 80
private val notesByLocale = mapOf(
    "en-US" to "Six days of \"it is good\", and the first thing called not good is being alone. " +
        "That is not the order I expected.",
    "pt-BR" to "Seis dias de \"era bom\", e a primeira coisa chamada de não boa é estar sozinho. " +
        "Não era essa a ordem que eu esperava.",
    "es" to "Seis días de \"era bueno\", y lo primero que se llama no bueno es estar solo. No " +
        "era ese el orden que yo esperaba.",
)
private val chapters = listOf(1, 2, 3)
private val readDate = LocalDate(year = 2026, monthNumber = 7, dayOfMonth = 20)

private fun passage() = PassageModel(
    bookId = BookId.GEN,
    chapters = chapters.map { chapter ->
        ChapterModel(
            number = chapter,
            startVerse = null,
            endVerse = null,
            bookId = BookId.GEN,
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
