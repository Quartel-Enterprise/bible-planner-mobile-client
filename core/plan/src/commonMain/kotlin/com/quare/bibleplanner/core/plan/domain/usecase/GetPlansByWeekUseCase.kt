package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.plan.ChapterPlanModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassagePlanModel
import com.quare.bibleplanner.core.model.plan.PlansModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

class GetPlansByWeekUseCase(
    private val planRepository: PlanRepository,
    private val booksRepository: BooksRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<PlansModel> = booksRepository.getBooksFlow().flatMapLatest { books ->
        val chronologicalOrder = planRepository.getPlans(ReadingPlanType.CHRONOLOGICAL)
        val booksOrder = planRepository.getPlans(ReadingPlanType.BOOKS)
        flow {
            emit(
                PlansModel(
                    chronologicalOrder = chronologicalOrder.map { weekPlan ->
                        weekPlan.copy(
                            days = weekPlan.days.map { day ->
                                calculateDayReadStatus(day, books)
                            },
                        )
                    },
                    booksOrder = booksOrder.map { weekPlan ->
                        weekPlan.copy(
                            days = weekPlan.days.map { day ->
                                calculateDayReadStatus(day, books)
                            },
                        )
                    },
                ),
            )
        }
    }

    private fun calculateDayReadStatus(
        day: DayModel,
        books: List<BookDataModel>,
    ): DayModel {
        val updatedBooks = day.passages.map { passage ->
            calculatePassageReadStatus(passage, books)
        }
        val isDayRead = updatedBooks.all { it.isRead }

        return day.copy(
            passages = updatedBooks,
            isRead = isDayRead,
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
}
