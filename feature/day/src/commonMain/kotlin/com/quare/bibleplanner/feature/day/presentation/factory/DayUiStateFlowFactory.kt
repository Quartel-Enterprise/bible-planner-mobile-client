package com.quare.bibleplanner.feature.day.presentation.factory

import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.plan.ChapterPlanModel
import com.quare.bibleplanner.core.model.plan.PassagePlanModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.feature.day.domain.EditDaySelectableDates
import com.quare.bibleplanner.feature.day.domain.usecase.CalculateAllChaptersReadStatusUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.ConvertTimestampToDatePickerInitialDateUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.GetBooksUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.GetDayDetailsUseCase
import com.quare.bibleplanner.feature.day.presentation.mapper.ReadDateFormatter
import com.quare.bibleplanner.feature.day.presentation.model.DatePickerUiState
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class DayUiStateFlowFactory(
    private val getDayDetails: GetDayDetailsUseCase,
    private val getBooks: GetBooksUseCase,
    private val readDateFormatter: ReadDateFormatter,
    private val editDaySelectableDates: EditDaySelectableDates,
    private val convertTimestampToDatePickerInitialDate: ConvertTimestampToDatePickerInitialDateUseCase,
    private val calculateAllChaptersReadStatus: CalculateAllChaptersReadStatusUseCase,
    private val localDateTimeProvider: LocalDateTimeProvider,
) {
    fun createUiState(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
        currentState: DayUiState.Loaded?,
    ): Flow<DayUiState> = combine(
        getDayDetails(weekNumber, dayNumber, readingPlanType),
        getBooks(),
    ) { day, books ->
        if (day != null) {
            val existingDatePickerUiState = currentState?.datePickerUiState

            // Calculate initial timestamp and date components
            val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
            val savedTimestamp = day.readTimestamp ?: currentTimeMillis
            // Convert to UTC midnight for DatePicker (DatePicker expects UTC midnight)
            val initialTimestamp = convertTimestampToDatePickerInitialDate(savedTimestamp)
            val initialDate = localDateTimeProvider.getLocalDateTime(savedTimestamp)

            val datePickerUiState = if (existingDatePickerUiState?.selectableDates != null) {
                existingDatePickerUiState.copy(
                    initialTimestamp = initialTimestamp,
                    initialHour = initialDate.hour,
                    initialMinute = initialDate.minute,
                )
            } else {
                (
                    existingDatePickerUiState ?: DatePickerUiState(
                        visiblePicker = null,
                        selectedDateMillis = null,
                        selectedLocalDate = null,
                        selectableDates = editDaySelectableDates,
                        initialTimestamp = initialTimestamp,
                        initialHour = initialDate.hour,
                        initialMinute = initialDate.minute,
                    )
                ).copy(
                    selectableDates = editDaySelectableDates,
                    initialTimestamp = initialTimestamp,
                    initialHour = initialDate.hour,
                    initialMinute = initialDate.minute,
                )
            }

            val (completedCount, totalCount) = calculatePassageCounts(
                passages = day.passages,
                books = books,
            )

            DayUiState.Loaded(
                day = day,
                weekNumber = weekNumber,
                books = books,
                datePickerUiState = datePickerUiState,
                formattedReadDate = day.readTimestamp?.let(readDateFormatter::format),
                chapterReadStatus = calculateAllChaptersReadStatus(
                    passages = day.passages,
                    books = books,
                ),
                completedPassagesCount = completedCount,
                totalPassagesCount = totalCount,
            )
        } else {
            DayUiState.Loading
        }
    }

    /**
     * Calculate the total number of chapters/items displayed and how many are completed.
     * Returns a Pair of (completedCount, totalCount).
     */
    private fun calculatePassageCounts(
        passages: List<PassagePlanModel>,
        books: List<BookDataModel>,
    ): Pair<Int, Int> {
        var totalCount = 0
        var completedCount = 0

        passages.forEach { passage ->
            if (passage.chapters.isEmpty()) {
                // If no chapters specified, count as 1 item (the whole book)
                totalCount++
                if (passage.isRead) {
                    completedCount++
                }
            } else {
                // Count each chapter as a separate item
                passage.chapters.forEach { chapter ->
                    totalCount++
                    val isChapterRead = isChapterReadForCount(
                        passage = passage,
                        chapter = chapter,
                        books = books,
                    )
                    if (isChapterRead) {
                        completedCount++
                    }
                }
            }
        }

        return Pair(completedCount, totalCount)
    }

    /**
     * Check if a specific chapter within a passage is read by checking the book data.
     */
    private fun isChapterReadForCount(
        passage: PassagePlanModel,
        chapter: ChapterPlanModel,
        books: List<BookDataModel>,
    ): Boolean {
        val book = books.find { it.id == passage.bookId } ?: return false
        val bookChapter = book.chapters.find { it.number == chapter.number } ?: return false

        val startVerse = chapter.startVerse
        val endVerse = chapter.endVerse

        return when {
            // If verse range is specified, check those specific verses
            startVerse != null && endVerse != null -> {
                val requiredVerses = startVerse..endVerse
                requiredVerses.all { verseNumber ->
                    bookChapter.verses.find { it.number == verseNumber }?.isRead == true
                }
            }

            // If only start verse is specified, check from that verse to end of chapter
            startVerse != null -> {
                bookChapter.verses
                    .filter { it.number >= startVerse }
                    .all { it.isRead }
            }

            // If no verse range specified, check if entire chapter is read
            else -> {
                bookChapter.isRead
            }
        }
    }
}
