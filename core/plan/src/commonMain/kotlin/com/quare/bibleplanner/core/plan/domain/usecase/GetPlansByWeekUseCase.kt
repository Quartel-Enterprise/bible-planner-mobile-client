package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.plan.ChapterPlanModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassagePlanModel
import com.quare.bibleplanner.core.model.plan.PlansModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate

class GetPlansByWeekUseCase(
    private val planRepository: PlanRepository,
    private val booksRepository: BooksRepository,
    private val getPlannedReadDateForDayUseCase: GetPlannedReadDateForDayUseCase,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<PlansModel> = combine(
        booksRepository.getBooksFlow(),
        planRepository.getStartPlanTimestamp(),
    ) { books, startDate ->
        books to startDate
    }.flatMapLatest { (books: List<BookDataModel>, startDate: LocalDate?) ->
        flow {
            emit(
                PlansModel(
                    chronologicalOrder = planRepository
                        .getPlans(ReadingPlanType.CHRONOLOGICAL)
                        .toUpdatedDayStatus(books, startDate),
                    booksOrder = planRepository
                        .getPlans(ReadingPlanType.BOOKS)
                        .toUpdatedDayStatus(books, startDate),
                ),
            )
        }
    }

    private fun List<WeekPlanModel>.toUpdatedDayStatus(
        books: List<BookDataModel>,
        startDate: LocalDate?,
    ): List<WeekPlanModel> = map { weekPlan ->
        weekPlan.copy(
            days = weekPlan.days.map { day ->
                calculateDayReadStatus(
                    day = day,
                    books = books,
                    weekNumber = weekPlan.number,
                    startDate = startDate,
                )
            },
        )
    }

    private fun calculateDayReadStatus(
        day: DayModel,
        books: List<BookDataModel>,
        weekNumber: Int,
        startDate: LocalDate?,
    ): DayModel {
        val updatedBooks = day.passages.map { passage ->
            calculatePassageReadStatus(passage, books)
        }
        return day.copy(
            passages = updatedBooks,
            isRead = updatedBooks.all { it.isRead },
            totalVerses = calculateTotalVerses(day.passages, books),
            readVerses = calculateReadVerses(day.passages, books),
            readTimestamp = day.readTimestamp,
            plannedReadDate = startDate?.let {
                getPlannedReadDateForDayUseCase(
                    weekNumber = weekNumber,
                    dayNumber = day.number,
                    startDate = it,
                )
            },
        )
    }

    private fun calculatePassageReadStatus(
        passage: PassagePlanModel,
        books: List<BookDataModel>,
    ): PassagePlanModel {
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
        chapterPlan: ChapterPlanModel,
        book: BookDataModel,
    ): Boolean {
        val chapter = book.chapters.find { it.number == chapterPlan.number }
            ?: return false

        val startVerse = chapterPlan.startVerse
        val endVerse = chapterPlan.endVerse

        return when {
            // If verse range is specified, check those specific verses
            startVerse != null && endVerse != null -> {
                val requiredVerses = startVerse..endVerse
                requiredVerses.all { verseNumber ->
                    chapter.verses.find { it.number == verseNumber }?.isRead == true
                }
            }

            // If only start verse is specified, check from that verse to end of chapter
            startVerse != null -> {
                chapter.verses
                    .filter { it.number >= startVerse }
                    .all { it.isRead }
            }

            // If no verse range specified, check if entire chapter is read
            else -> {
                chapter.isRead
            }
        }
    }

    private fun calculateTotalVerses(
        passages: List<PassagePlanModel>,
        books: List<BookDataModel>,
    ): Int = passages.sumOf { passage ->
        calculatePassageTotalVerses(passage, books)
    }

    private fun calculatePassageTotalVerses(
        passage: PassagePlanModel,
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
        chapterPlan: ChapterPlanModel,
        book: BookDataModel,
    ): Int {
        val chapter = book.chapters.find { it.number == chapterPlan.number }
            ?: return 0

        val startVerse = chapterPlan.startVerse
        val endVerse = chapterPlan.endVerse

        return when {
            // If verse range is specified, count those specific verses
            startVerse != null && endVerse != null -> {
                endVerse - startVerse + 1
            }

            // If only start verse is specified, count from that verse to end of chapter
            startVerse != null -> {
                chapter.verses.count { it.number >= startVerse }
            }

            // If no verse range specified, count all verses in the chapter
            else -> {
                chapter.verses.size
            }
        }
    }

    private fun calculateReadVerses(
        passages: List<PassagePlanModel>,
        books: List<BookDataModel>,
    ): Int = passages.sumOf { passage ->
        calculatePassageReadVerses(passage, books)
    }

    private fun calculatePassageReadVerses(
        passage: PassagePlanModel,
        books: List<BookDataModel>,
    ): Int {
        val book = books.find { it.id == passage.bookId } ?: return 0

        // If no chapters specified (empty list), count all read verses in the book
        if (passage.chapters.isEmpty()) {
            return book.chapters.sumOf { chapter ->
                chapter.verses.count { it.isRead }
            }
        }

        return passage.chapters.sumOf { chapterPlan ->
            calculateChapterPlanReadVerses(chapterPlan, book)
        }
    }

    private fun calculateChapterPlanReadVerses(
        chapterPlan: ChapterPlanModel,
        book: BookDataModel,
    ): Int {
        val chapter = book.chapters.find { it.number == chapterPlan.number }
            ?: return 0

        val startVerse = chapterPlan.startVerse
        val endVerse = chapterPlan.endVerse

        return when {
            // If verse range is specified, count read verses in that range
            startVerse != null && endVerse != null -> {
                chapter.verses.count { verse ->
                    verse.number in startVerse..endVerse && verse.isRead
                }
            }

            // If only start verse is specified, count read verses from that verse to end of chapter
            startVerse != null -> {
                chapter.verses.count { verse ->
                    verse.number >= startVerse && verse.isRead
                }
            }

            // If no verse range specified, count all read verses in the chapter
            else -> {
                chapter.verses.count { it.isRead }
            }
        }
    }
}
