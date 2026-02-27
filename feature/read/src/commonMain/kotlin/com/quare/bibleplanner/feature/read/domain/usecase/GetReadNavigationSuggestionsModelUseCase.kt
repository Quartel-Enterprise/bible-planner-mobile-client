package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.PlansModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetReadNavigationSuggestionsModelUseCase(
    private val getPlansByWeek: GetPlansByWeekUseCase,
    private val planRepository: PlanRepository,
    private val booksRepository: BooksRepository,
) {
    /**
     * Get read navigation suggestions model
     * @param shouldForceCanonOrder if true we should get the model from the canon order, if not we should verify the selected plan
     * @param currentBookId the book id reference to calculate next and previous
     * @param currentChapterNumber the chapter number reference to calculate next and previous
     */
    operator fun invoke(
        shouldForceCanonOrder: Boolean,
        currentBookId: BookId,
        currentChapterNumber: Int,
    ): Flow<ReadNavigationSuggestionsModel> = combine(
        getPlansByWeek(),
        planRepository.getSelectedReadingPlanFlow(),
        booksRepository.getBooksFlow(),
    ) { plans, selectedPlanType, books ->
        val activePlanType = if (shouldForceCanonOrder) {
            ReadingPlanType.BOOKS
        } else {
            selectedPlanType
        }

        val flattenedPlan = plans
            .toOrder(activePlanType)
            .flatMap { it.days }
            .flatMap { it.passages }
            .flatMap { passage ->
                val bookId = passage.bookId
                if (passage.chapters.isEmpty()) {
                    val book = books.find { it.id == bookId }
                    book
                        ?.chapters
                        ?.map { chapter ->
                            ReadNavigationSuggestionModel(
                                bookId = bookId,
                                chapterNumber = chapter.number,
                            )
                        }.orEmpty()
                } else {
                    passage.chapters.map { chapter ->
                        ReadNavigationSuggestionModel(
                            bookId = bookId,
                            chapterNumber = chapter.number,
                        )
                    }
                }
            }

        val currentIndex = flattenedPlan.indexOfFirst {
            it.bookId == currentBookId && it.chapterNumber == currentChapterNumber
        }

        if (currentIndex == -1) {
            ReadNavigationSuggestionsModel(
                previous = null,
                next = null,
            )
        } else {
            ReadNavigationSuggestionsModel(
                previous = flattenedPlan.getOrNull(currentIndex - 1),
                next = flattenedPlan.getOrNull(currentIndex + 1),
            )
        }
    }

    private fun PlansModel.toOrder(plan: ReadingPlanType): List<WeekPlanModel> = when (plan) {
        ReadingPlanType.CHRONOLOGICAL -> chronologicalOrder
        ReadingPlanType.BOOKS -> booksOrder
    }
}
