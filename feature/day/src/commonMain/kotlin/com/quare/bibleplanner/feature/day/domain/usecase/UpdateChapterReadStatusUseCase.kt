package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.UpdatePassageReadStatusUseCase
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.repository.DayRepository
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.feature.day.domain.model.UpdateReadStatusOfPassageStrategy
import kotlinx.coroutines.flow.first

class UpdateChapterReadStatusUseCase(
    private val dayRepository: DayRepository,
    private val markPassagesRead: UpdatePassageReadStatusUseCase,
    private val getPlansByWeek: GetPlansByWeekUseCase,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {
    suspend operator fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        strategy: UpdateReadStatusOfPassageStrategy,
        isRead: Boolean,
        readingPlanType: ReadingPlanType,
    ): Result<Unit> {
        val errorResult = Result.failure<Unit>(IllegalStateException())
        val plansModel = getPlansByWeek().first()
        val weeks = when (readingPlanType) {
            ReadingPlanType.CHRONOLOGICAL -> plansModel.chronologicalOrder
            ReadingPlanType.BOOKS -> plansModel.booksOrder
        }
        val week = weeks.find { it.number == weekNumber } ?: return errorResult
        val day = week.days.find { it.number == dayNumber } ?: return errorResult
        val passageIndex = strategy.passageIndex

        if (passageIndex < 0 || passageIndex >= day.passages.size) return errorResult

        val passage = day.passages[passageIndex]

        val passageToUpdate = when (strategy) {
            is UpdateReadStatusOfPassageStrategy.Chapter -> {
                val chapterIndex = strategy.chapterIndex
                if (chapterIndex < 0 || chapterIndex >= passage.chapters.size) return errorResult
                val chapter = passage.chapters[chapterIndex]
                PassageModel(
                    bookId = passage.bookId,
                    chapters = listOf(chapter),
                    isRead = false,
                    chapterRanges = passage.chapterRanges,
                )
            }

            is UpdateReadStatusOfPassageStrategy.EntireBook -> passage
        }

        markPassagesRead(passageToUpdate)

        val updatedPlansModel = getPlansByWeek().first()
        val updatedWeeks = when (readingPlanType) {
            ReadingPlanType.CHRONOLOGICAL -> updatedPlansModel.chronologicalOrder
            ReadingPlanType.BOOKS -> updatedPlansModel.booksOrder
        }
        val updatedWeek = updatedWeeks.find { it.number == weekNumber } ?: return errorResult
        val updatedDay = updatedWeek.days.find { it.number == dayNumber } ?: return errorResult

        val allPassagesRead = updatedDay.passages.all { it.isRead }
        dayRepository.run {
            if (allPassagesRead) {
                val readTimestamp = currentTimestampProvider.getCurrentTimestamp()
                updateDayReadStatus(
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                    readingPlanType = readingPlanType,
                    isRead = true,
                    readTimestamp = readTimestamp,
                )
            } else {
                updateDayReadStatus(
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                    readingPlanType = readingPlanType,
                    isRead = false,
                    readTimestamp = null,
                )
            }
        }
        return Result.success(Unit)
    }
}
