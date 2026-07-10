package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.books.domain.isRangeRead
import com.quare.bibleplanner.core.books.domain.isVerseRead
import com.quare.bibleplanner.core.books.domain.readVersesCount
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.PlansModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class GetPlansByWeekUseCase(
    private val planRepository: PlanRepository,
    private val booksRepository: BooksRepository,
    private val getPlannedReadDateForDayUseCase: GetPlannedReadDateForDayUseCase,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val localDateTimeProvider: LocalDateTimeProvider,
) {
    operator fun invoke(): Flow<PlansModel> {
        val today = getTodayLocalDate()
        return combine(
            booksRepository.getBooksFlow(),
            planRepository.getStartPlanTimestamp(),
        ) { books, startDate ->
            books to startDate
        }.map { (books, startDate) ->
            PlansModel(
                chronologicalOrder = planRepository
                    .getPlans(ReadingPlanType.CHRONOLOGICAL)
                    .toUpdatedDayStatus(books, startDate, today),
                booksOrder = planRepository
                    .getPlans(ReadingPlanType.BOOKS)
                    .toUpdatedDayStatus(books, startDate, today),
            )
        }.flowOn(Dispatchers.Default)
    }

    private fun getTodayLocalDate(): LocalDate = localDateTimeProvider
        .getLocalDateTime(currentTimestampProvider.getCurrentTimestamp())
        .date

    private fun List<WeekPlanModel>.toUpdatedDayStatus(
        books: List<BookDataModel>,
        startDate: LocalDate?,
        today: LocalDate,
    ): List<WeekPlanModel> = map { weekPlan ->
        weekPlan.copy(
            days = weekPlan.days.map { day ->
                calculateDayReadStatus(
                    day = day,
                    books = books,
                    weekNumber = weekPlan.number,
                    startDate = startDate,
                    today = today,
                )
            },
        )
    }

    private fun calculateDayReadStatus(
        day: DayModel,
        books: List<BookDataModel>,
        weekNumber: Int,
        startDate: LocalDate?,
        today: LocalDate,
    ): DayModel {
        val updatedBooks = day.passages.map { passage ->
            calculatePassageReadStatus(passage, books)
        }
        val plannedReadDate = startDate?.let {
            getPlannedReadDateForDayUseCase(
                weekNumber = weekNumber,
                dayNumber = day.number,
                startDate = it,
            )
        }
        return day.copy(
            passages = updatedBooks,
            isRead = updatedBooks.all { it.isRead },
            totalVerses = calculateTotalVerses(day.passages, books),
            readVerses = calculateReadVerses(day.passages, books),
            readTimestamp = day.readTimestamp,
            plannedReadDate = plannedReadDate,
            isToday = plannedReadDate == today,
        )
    }

    private fun calculatePassageReadStatus(
        passage: PassageModel,
        books: List<BookDataModel>,
    ): PassageModel {
        val book = books.find { it.id == passage.bookId } ?: return passage.copy(isRead = false)

        // If no chapters specified (empty list), check if entire book is read
        val allChaptersRead = if (passage.chapters.isEmpty()) {
            book.isRead
        } else {
            passage.chapters.all { chapterPlan ->
                isChapterPlanRead(chapterPlan, book)
            }
        }

        return passage.copy(isRead = allChaptersRead)
    }

    private fun isChapterPlanRead(
        chapterPlan: ChapterModel,
        book: BookDataModel,
    ): Boolean {
        val chapter = book.chapters.find { it.number == chapterPlan.number }
            ?: return false
        return chapter.isRangeRead(chapterPlan.startVerse, chapterPlan.endVerse)
    }

    private fun calculateTotalVerses(
        passages: List<PassageModel>,
        books: List<BookDataModel>,
    ): Int = passages.sumOf { passage ->
        calculatePassageTotalVerses(passage, books)
    }

    private fun calculatePassageTotalVerses(
        passage: PassageModel,
        books: List<BookDataModel>,
    ): Int {
        val book = books.find { it.id == passage.bookId } ?: return 0

        // If no chapters specified (empty list), count all verses in the book
        if (passage.chapters.isEmpty()) {
            return book.chapters.sumOf { it.verses.size }
        }

        return passage.chapters.sumOf { chapterPlan ->
            calculateChapterPlanTotalVerses(chapterPlan, book)
        }
    }

    private fun calculateChapterPlanTotalVerses(
        chapterPlan: ChapterModel,
        book: BookDataModel,
    ): Int {
        val chapter = book.chapters.find { it.number == chapterPlan.number }
            ?: return 0

        val startVerse = chapterPlan.startVerse
        val endVerse = chapterPlan.endVerse

        return when {
            // If verse range is specified, count those specific verses
            startVerse != null && endVerse != null -> endVerse - startVerse + 1

            // If only start verse is specified, count from that verse to end of chapter
            startVerse != null -> chapter.verses.count { it.number >= startVerse }

            // If no verse range specified, count all verses in the chapter
            else -> chapter.verses.size
        }
    }

    private fun calculateReadVerses(
        passages: List<PassageModel>,
        books: List<BookDataModel>,
    ): Int = passages.sumOf { passage ->
        calculatePassageReadVerses(passage, books)
    }

    private fun calculatePassageReadVerses(
        passage: PassageModel,
        books: List<BookDataModel>,
    ): Int {
        val book = books.find { it.id == passage.bookId } ?: return 0

        // If no chapters specified (empty list), count all read verses in the book
        if (passage.chapters.isEmpty()) {
            return book.chapters.sumOf { it.readVersesCount }
        }

        return passage.chapters.sumOf { chapterPlan ->
            calculateChapterPlanReadVerses(chapterPlan, book)
        }
    }

    private fun calculateChapterPlanReadVerses(
        chapterPlan: ChapterModel,
        book: BookDataModel,
    ): Int {
        val chapter = book.chapters.find { it.number == chapterPlan.number }
            ?: return 0

        val startVerse = chapterPlan.startVerse
        val endVerse = chapterPlan.endVerse

        return when {
            // If verse range is specified, count read verses in that range
            startVerse != null && endVerse != null ->
                (startVerse..endVerse).count { verseNumber -> chapter.isVerseRead(verseNumber) }

            // If only start verse is specified, count read verses from that verse to end of chapter
            startVerse != null -> {
                chapter.verses.count { verse ->
                    verse.number >= startVerse && chapter.isVerseRead(verse.number)
                }
            }

            // If no verse range specified, count all read verses in the chapter
            else -> chapter.readVersesCount
        }
    }
}
