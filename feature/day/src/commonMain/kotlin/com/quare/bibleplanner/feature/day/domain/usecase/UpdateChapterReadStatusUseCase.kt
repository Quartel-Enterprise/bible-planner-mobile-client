package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.MarkPassagesReadUseCase
import com.quare.bibleplanner.core.model.plan.PassagePlanModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.repository.DayRepository
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class UpdateChapterReadStatusUseCase(
    private val dayRepository: DayRepository,
    private val markPassagesReadUseCase: MarkPassagesReadUseCase,
    private val getPlansByWeekUseCase: GetPlansByWeekUseCase,
) {
    suspend operator fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        passageIndex: Int,
        chapterIndex: Int,
        isRead: Boolean,
        readingPlanType: ReadingPlanType = ReadingPlanType.CHRONOLOGICAL,
    ) {
        // Get the day to access its passages
        val plansModel = getPlansByWeekUseCase().first()
        val weeks = when (readingPlanType) {
            ReadingPlanType.CHRONOLOGICAL -> plansModel.chronologicalOrder
            ReadingPlanType.BOOKS -> plansModel.booksOrder
        }
        val week = weeks.find { it.number == weekNumber } ?: return
        val day = week.days.find { it.number == dayNumber } ?: return

        if (passageIndex < 0 || passageIndex >= day.passages.size) return

        val passage = day.passages[passageIndex]

        // If chapterIndex is -1, it means the passage has no chapters (entire book)
        // Otherwise, create a passage with just the specific chapter
        val chapterToUpdate = if (chapterIndex == -1) {
            // Update the entire book
            passage
        } else {
            if (chapterIndex < 0 || chapterIndex >= passage.chapters.size) return
            val chapter = passage.chapters[chapterIndex]
            // Create a passage with just this chapter
            PassagePlanModel(
                bookId = passage.bookId,
                chapters = listOf(chapter),
                isRead = false,
                chapterRanges = passage.chapterRanges,
            )
        }

        // Update the specific chapter
        markPassagesReadUseCase(listOf(chapterToUpdate))

        // Check if all passages are now read
        val updatedPlansModel = getPlansByWeekUseCase().first()
        val updatedWeeks = when (readingPlanType) {
            ReadingPlanType.CHRONOLOGICAL -> updatedPlansModel.chronologicalOrder
            ReadingPlanType.BOOKS -> updatedPlansModel.booksOrder
        }
        val updatedWeek = updatedWeeks.find { it.number == weekNumber } ?: return
        val updatedDay = updatedWeek.days.find { it.number == dayNumber } ?: return

        // If all passages are read, update day read status
        val allPassagesRead = updatedDay.passages.all { it.isRead }
        if (allPassagesRead) {
            val readTimestamp = Clock.System.now().toEpochMilliseconds()
            dayRepository.updateDayReadStatus(
                weekNumber = weekNumber,
                dayNumber = dayNumber,
                isRead = true,
                readTimestamp = readTimestamp,
            )
        } else {
            // If not all passages are read, unmark day as read
            dayRepository.updateDayReadStatus(
                weekNumber = weekNumber,
                dayNumber = dayNumber,
                isRead = false,
                readTimestamp = null,
            )
        }
    }
}
