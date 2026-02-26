package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.UpdatePassageReadStatusUseCase
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.repository.DayRepository
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.feature.day.domain.model.UpdateReadStatusOfPassageStrategy
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalTime::class)
class UpdateChapterReadStatusUseCase(
    private val dayRepository: DayRepository,
    private val markPassagesRead: UpdatePassageReadStatusUseCase,
    private val getPlansByWeek: GetPlansByWeekUseCase,
) {
    suspend operator fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        strategy: UpdateReadStatusOfPassageStrategy,
        isRead: Boolean,
        readingPlanType: ReadingPlanType,
    ): Result<Unit> {
        // Get the day to access its passages
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

        // If chapterIndex is null, it means the passage has no chapters (entire book)
        // Otherwise, create a passage with just the specific chapter
        val passageToUpdate = when (strategy) {
            is UpdateReadStatusOfPassageStrategy.Chapter -> {
                val chapterIndex = strategy.chapterIndex
                if (chapterIndex < 0 || chapterIndex >= passage.chapters.size) return errorResult
                val chapter = passage.chapters[chapterIndex]
                // Create a passage with just this chapter
                PassageModel(
                    bookId = passage.bookId,
                    chapters = listOf(chapter),
                    isRead = false,
                    chapterRanges = passage.chapterRanges,
                )
            }

            is UpdateReadStatusOfPassageStrategy.EntireBook -> passage
        }

        // Update the specific chapter
        markPassagesRead(passageToUpdate)

        // Check if all passages are now read
        val updatedPlansModel = getPlansByWeek().first()
        val updatedWeeks = when (readingPlanType) {
            ReadingPlanType.CHRONOLOGICAL -> updatedPlansModel.chronologicalOrder
            ReadingPlanType.BOOKS -> updatedPlansModel.booksOrder
        }
        val updatedWeek = updatedWeeks.find { it.number == weekNumber } ?: return errorResult
        val updatedDay = updatedWeek.days.find { it.number == dayNumber } ?: return errorResult

        // If all passages are read, update day read status
        val allPassagesRead = updatedDay.passages.all { it.isRead }
        dayRepository.run {
            if (allPassagesRead) {
                val readTimestamp = Clock.System.now().toEpochMilliseconds()
                updateDayReadStatus(
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                    readingPlanType = readingPlanType,
                    isRead = true,
                    readTimestamp = readTimestamp,
                )
            } else {
                // If not all passages are read, unmark day as read
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
